package com.yunji.titanrtx.manager.service.report;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.*;
import com.yunji.titanrtx.common.alarm.AlarmService;
import com.yunji.titanrtx.common.alarm.MessageSender;
import com.yunji.titanrtx.common.annotation.TaskAnnotation;
import com.yunji.titanrtx.common.domain.statistics.*;
import com.yunji.titanrtx.common.domain.task.Pair;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.task.Report;
import com.yunji.titanrtx.common.u.*;
import com.yunji.titanrtx.manager.dao.bos.http.ReportRecordsBo;
import com.yunji.titanrtx.manager.dao.entity.data.BeaconAlertEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.service.common.SystemProperties;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import com.yunji.titanrtx.manager.service.report.alert.AlertReportGenerator;
import com.yunji.titanrtx.manager.service.report.dto.*;
import com.yunji.titanrtx.manager.service.report.service.BeaconCollectorService;
import com.yunji.titanrtx.manager.service.report.service.ScreenService;
import com.yunji.titanrtx.manager.service.report.support.*;
import com.yunji.titanrtx.manager.service.support.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.yunji.titanrtx.common.u.LocalDateU.*;
import static com.yunji.titanrtx.manager.service.report.support.ReportUtils.*;
import static com.yunji.titanrtx.manager.service.report.support.ReportLog.log;

/**
 * 自动化压测报告,主要收集云集内部grafana 监控指标。
 *
 * @author leihz
 * @since 2020-05-11
 */
@TaskAnnotation(type = TaskType.OUTSIDE)
public class AutoReportHandler implements Report {

    private final MetricsCollector reportCollector;

    private final BeaconCollectorService beaconService;

    @Resource
    private HttpSceneService httpSceneService;
    @Resource
    private AlarmService alarmService;

    @Resource
    private SystemProperties systemProperties;

    @Resource
    private ScreenService screenService;


    public AutoReportHandler(MetricsCollector reportCollector, BeaconCollectorService beaconService) {
        this.reportCollector = reportCollector;
        this.beaconService = beaconService;
    }


    @Override
    public void report(Statistics statistics) {

        if (CommonU.isIDC()) {
            log.info(".....AutoReportHandler,自动压测报告指标数据,taskNo:{}.....", statistics.getTaskNo());
            long startTime = statistics.getStartTime().getTime();
            long endTime = statistics.getEndTime().getTime();
            OutsideStatistics outsideStatistics = new OutsideStatistics(statistics, startTime, endTime);
            reportOutside(outsideStatistics);
        }
    }

    /**
     * report.
     */
    public void reportOutside(OutsideStatistics osStats) {
        long st = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(4);
        //最终输出对象
        FinalReportDTO finalDTO = new FinalReportDTO();
        try {
            //CGI数据
            List<ReportCGIMetrics> cgiLinkMetrics = new ArrayList<>();
            //dubbo耗时数据
            List<ReportDubboTopMetrics> dubboTopCostMetrics = new ArrayList<>();
            //beacon 异常告警数据
            List<BeaconAlertEntity> beaconAlertMetrics = new ArrayList<>();

            supplierAsync(() -> reportCollector.collectLinkCGIMetric(osStats), ret -> {
                latch.countDown();
                if (ret != null) {
                    cgiLinkMetrics.addAll(ret);
                }
            });
            supplierAsync(() -> reportCollector.collectTopDubboCostMetric(osStats), ret -> {
                latch.countDown();
                if (ret != null) {
                    dubboTopCostMetrics.addAll(ret);
                }
            });

            if (CommonU.isIDC()) {
                supplierAsync(() -> beaconService.collectBeaconAlerts(osStats), ret -> {
                    latch.countDown();
                    if (ret != null) {
                        beaconAlertMetrics.addAll(ret);
                    }
                });
            }
            //截图服务.
            supplierAsync(() -> screenService.screenshotMetric(osStats.getStatistics().getTaskNo(), finalDTO), ret -> {
                latch.countDown();
            });
            latch.await();

            finalDTO.buildBizWrappers();

            log.info("[Report]并行收集指标结束,cgi-size:{},dubbo-cost-size:{},beacon-size:{},cost:{}",
                    cgiLinkMetrics.size(), dubboTopCostMetrics.size(), beaconAlertMetrics.size(),
                    System.currentTimeMillis() - st);

            generateReport(osStats, finalDTO, cgiLinkMetrics, dubboTopCostMetrics, beaconAlertMetrics);
            sendReport(osStats);
        } catch (Exception e) {
            for (long i = 0; i < latch.getCount(); i++) {
                latch.countDown();
            }
            log.error("ReportOutside error: " + e.getMessage());
        }
    }

    private void generateReport(OutsideStatistics os,
                                FinalReportDTO finalDTO,
                                List<ReportCGIMetrics> cgiLinkMetrics,
                                List<ReportDubboTopMetrics> dubboTopCostMetrics,
                                List<BeaconAlertEntity> beaconAlertMetrics) {

        String startTime = getNormalDate(os.getStartTime());
        String endTime = getNormalDate(os.getEndTime());

        log.info("Out report: startTime:{},endTime:{},cgiMetrics size:{},dubboCostMetric size:{},beacon alert size: {}",
                startTime, endTime, cgiLinkMetrics.size(), dubboTopCostMetrics.size(), beaconAlertMetrics.size());

        finalDTO.setStartDate(startTime);
        finalDTO.setEndDate(endTime);

        Configure config = Configure.newBuilder()
                .customPolicy("beaconAlertReport", new RecordsDetailTablePolicy(3))
                .build();
        try {
            //beacon alert data.
            ReportRecordsBo beaconAlertRecords = fetchBeaconAlertRecord(beaconAlertMetrics);
            finalDTO.setBeaconAlertReport(beaconAlertRecords);


            XWPFTemplate template = XWPFTemplate.compile(systemProperties.templatePath, config).render(finalDTO);

            String outFilePath = generateReportName(os.getStartTime(), false);

            FileOutputStream outputStream = new FileOutputStream(outFilePath);

            template.write(outputStream);
            template.close();
            log.info("......生成压测报告成功,存储路径:{}", outFilePath);
        } catch (Exception e) {
            log.error("生成压测报告失败: " + e.getMessage(), e);
        }
    }

    private String generateReportName(long time, boolean isFileName) {
        if (isFileName) {
            return getFileDayTime(time) + REPORT_SUFFIX;
        }
        return systemProperties.outputPathPrefix + getFileDayTime(time) + REPORT_SUFFIX;
    }

    private void sendReport(OutsideStatistics os) {
        String fileName = generateReportName(os.getStartTime(), true);

        Integer sceneId = CommonU.parseTaskNoToId(os.getStatistics().getTaskNo());
        Pair<String, String> alertInfo = httpSceneService.getAlertInfo(sceneId);
        log.info("场景{} alertInfo:{}", sceneId, alertInfo);

        boolean alertMulti = StringUtils.isEmpty(alertInfo.getKey());
        reportToQiWei(os, fileName, alertMulti);
    }

    private static ReportRecordsBo fetchDubboCostReportRecord(List<ReportDubboTopMetrics> dubboTopCostMetrics) {
        List<RowRenderData> dubboDataList = new ArrayList<>();

        int size = dubboTopCostMetrics.size();
        if (size > 20) size = 20;
        for (int i = 0; i < size; i++) {
            ReportDubboTopMetrics metric = dubboTopCostMetrics.get(i);
            int avgCost = CommonU.roundToInt(metric.getAvgElapsed());

            TextRenderData avgRenderData;
            if (avgCost > 300) {
                avgRenderData = textRenderRed(String.valueOf(CommonU.roundToInt(metric.getAvgElapsed())));
            } else {
                avgRenderData = textRenderCommon(String.valueOf(CommonU.roundToInt(metric.getAvgElapsed())));
            }

            RowRenderData renderData = RowRenderData.build(
                    textRenderCommon(LocalDateU.getHourMinuteTime(metric.getTime())),
                    textRenderCommon(String.valueOf(metric.getOwner())),
                    textRenderCommon(String.valueOf(metric.getApp())),
                    textRenderCommon(String.valueOf(metric.getService())),
                    textRenderCommon(String.valueOf(metric.getMethod())),
                    avgRenderData,
                    textRenderCommon(String.valueOf(metric.getSuccess())),
                    textRenderCommon(String.valueOf(metric.getSoftKey()))
            );
            dubboDataList.add(renderData);
        }
        //topCostMetrics.sort((o1, o2) -> (int) (o2.getSoftKey() - o1.getSoftKey()));
        ReportRecordsBo reportRecordsBo = new ReportRecordsBo();
        reportRecordsBo.setRecords(dubboDataList);

        return reportRecordsBo;
    }


    private static ReportRecordsBo fetchCGIReportRecord(List<ReportCGIMetrics> cgiLinkMetrics) {
        List<RowRenderData> cgiDataList = cgiLinkMetrics.stream().map(metric -> {
            //TextRenderData
            return RowRenderData.build(
                    textRenderCommon(String.valueOf(metric.getService())),
                    textRenderCommon(String.valueOf(metric.getTotalSuccess())),
                    textRenderCommon(String.valueOf(metric.getTotalFailure())),
                    textRenderCommon(String.valueOf(metric.getMaxConcurrent())),
                    textRenderCommon(String.valueOf(metric.getMaxElapsed())),
                    textRenderCommon(String.valueOf(metric.getAvgElapsed()))
            );
        }).collect(Collectors.toList());

        ReportRecordsBo reportRecordsBo = new ReportRecordsBo();
        reportRecordsBo.setRecords(cgiDataList);

        return reportRecordsBo;
    }

    private static ReportRecordsBo fetchBeaconAlertRecord(List<BeaconAlertEntity> beaconAlertMetrics) {
        List<RowRenderData> alertDataList = new ArrayList<>();

        for (int i = 0; i < beaconAlertMetrics.size(); i++) {
            BeaconAlertEntity metric = beaconAlertMetrics.get(i);

            RowRenderData renderData = RowRenderData.build(
                    textRenderCommon(metric.getAlertTime()),
                    textRenderCommon(metric.getAlertId()),
                    textRenderRed(metric.getMsg()));

            alertDataList.add(renderData);
        }
        ReportRecordsBo reportRecordsBo = new ReportRecordsBo();
        reportRecordsBo.setRecords(alertDataList);

        return reportRecordsBo;
    }

    /**
     * 将报告链接发送到企业微信
     */
    private void reportToQiWei(OutsideStatistics statistics, String fileName, boolean alertMulti) {
        log.info("报告fileName: {},taskNo:{}.", fileName, statistics.getStatistics().getTaskNo());

        Integer sceneId = CommonU.parseTaskNoToId(statistics.getStatistics().getTaskNo());
        HttpSceneEntity sceneEntity = httpSceneService.findById(sceneId);

        StringBuilder sb = new StringBuilder();

        String detailUrl = null;
        try {
            detailUrl = systemProperties.autoOutsideReportUrl + URLEncoder.encode(fileName, "utf-8");
        } catch (UnsupportedEncodingException ignored) {
        }

        sb.append("环境[").append(CommonU.getConfigEnv().toUpperCase()).append("]: Titan自动化压测报告 \n");
        sb.append("场景: [")
                .append(sceneId).append("-").append(sceneEntity.getName())
                .append("] 压测完成.\n\n")
                .append("收集grafana和监控告警信息完成\n")
                .append("链接报告: ").append(detailUrl).append("\n");

        CacheManager.put(detailUrl, System.currentTimeMillis() + "");

        String alarmContent = sb.toString();

        log.info("链路监控告警信息:{}", alarmContent);

        alarmService.send(MessageSender.Type.MAINTAINER, alarmContent);
        if (alertMulti) {
            alarmService.send(MessageSender.Type.AUTO_ALARM, alarmContent);
        }
    }
}
