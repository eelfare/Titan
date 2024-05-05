package com.yunji.titanrtx.manager.service.report.alert;

import com.yunji.titanrtx.common.alarm.AlarmService;
import com.yunji.titanrtx.common.alarm.MessageSender;
import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.common.domain.statistics.StatisticsDetail;
import com.yunji.titanrtx.common.domain.task.Pair;
import com.yunji.titanrtx.common.u.*;
import com.yunji.titanrtx.manager.dao.bos.*;
import com.yunji.titanrtx.manager.dao.bos.http.PairBo;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.http.*;
import com.yunji.titanrtx.manager.service.common.SystemProperties;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import com.yunji.titanrtx.manager.service.report.dto.AlertReportDTO;
import com.yunji.titanrtx.manager.service.report.dto.LinkStatisticsDTO;
import com.yunji.titanrtx.manager.service.report.support.MarkdownUtils;
import com.yunji.titanrtx.manager.service.report.support.ReportUtils;
import com.yunji.titanrtx.manager.service.support.CacheManager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yunji.titanrtx.manager.service.report.support.ReportUtils.*;

import static com.yunji.titanrtx.manager.service.report.support.ReportLog.log;

/**
 * 主要用来作监控链路告警使用的,最终生成告警报告.
 *
 * @author leihz
 * @since 2020-05-11
 */

@Component
public class AlertReportGenerator {
    @Resource
    private SystemProperties systemProperties;
    @Resource
    private HttpSceneService httpSceneService;
    @Resource
    private AlarmService alarmService;


    public void checkAndAlert(Integer sceneId, int reportId,
                              PressureReportBo pressureReportBo, Statistics statistics) {

        if (ReportUtils.notIDC()) {
            log.info("{}-当前环境非IDC,不需要告警.", getClass().getSimpleName());
            return;
        }

        Pair<String, String> alertInfo = httpSceneService.getAlertInfo(sceneId);
        String alertWebHook = alertInfo.getKey();
        if (StringUtils.isEmpty(alertWebHook)) {
            log.info("当前场景[{}] alert webHook 地址为空,不需要告警.", sceneId);
            return;
        }

        AlertReportDTO alertReportBo = new AlertReportDTO();
        List<LinkStatisticsDTO> detailBoList = new ArrayList<>();

        BaseEntity sceneEntity = pressureReportBo.getSceneEntity();
        if (sceneEntity instanceof HttpSceneEntity) {
            alertReportBo.setSceneName(((HttpSceneEntity) sceneEntity).getName());
        } else { //DubboSceneEntity
            alertReportBo.setSceneName(((DubboSceneEntity) sceneEntity).getName());
        }
        //LinkEntity
        List<LinkEntity> linkEntities = (List<LinkEntity>) pressureReportBo.getBulletEntity();
        Map<Integer, String> linkIdUrlMap = linkEntities
                .stream()
                .collect(Collectors.toMap(LinkEntity::getId, LinkEntity::getUrl));

        List<StatisticsBo> statisticsBoList = pressureReportBo.getSum().getBos();
        for (StatisticsBo statisticsBo : statisticsBoList) {
            LinkStatisticsDTO alertLinkDetailBo = new LinkStatisticsDTO();

            StatisticsDetail detailStatistics = statistics.getDetailStatistics(statisticsBo.getId());

            if (detailStatistics != null) {
                log.info("DetailStatistics:{}", detailStatistics);
                alertLinkDetailBo.setRetContents(detailStatistics.getWRONG_RET_CONTENTS());
            }

            alertLinkDetailBo.setLinkUrl(linkIdUrlMap.get(statisticsBo.getId()));

            alertLinkDetailBo.setLinkId(statisticsBo.getId());

            alertLinkDetailBo.setRequestTotal(statisticsBo.getRequestTotal());
            alertLinkDetailBo.setRequestSuccess(statisticsBo.getRequestSuccessCode());

            List<PairBo> pairBos = statisticsBo.getPairBos();
            for (PairBo pairBo : pairBos) {
                int code = pairBo.getCode();
                int times = pairBo.getTimes();
                if (code == 0) {
                    alertLinkDetailBo.setBizSuccessCode(times);
                } else if (code == 1000) {
                    alertLinkDetailBo.setBizErrorCode(times);
                } else {
                    alertLinkDetailBo.setBizOtherCode(times);
                }
            }
            detailBoList.add(alertLinkDetailBo);
        }

        alertReportBo.setStartTime(pressureReportBo.getStartTime().getTime());
        alertReportBo.setEndTime(pressureReportBo.getEndTime().getTime());
        alertReportBo.setDetailBoList(detailBoList);

        log.info("Alert web hook [{}] to report bo.", alertWebHook);

        alert2(alertReportBo, alertInfo, reportId);
    }

    /**
     * TITAN 链路监控告警
     */
    private void alert2(AlertReportDTO alertReportBo, Pair<String, String> alertInfo, int reportId) {
        StringBuilder sb = new StringBuilder();
        sb.append("##  \n");
        List<LinkStatisticsDTO> detailBoList = alertReportBo.getDetailBoList();

        int notGoodCount = 0;
        for (int i = 0; i < detailBoList.size(); i++) {
            LinkStatisticsDTO detailBo = detailBoList.get(i);
            //HTTP异常 零容忍.
            if (detailBo.getRequestTotal() - detailBo.getRequestSuccess() > 0) {
                buildAlertString(sb, detailBo);
                notGoodCount++;
                continue;
            }

            Double percentRate = MathU.calculatePercentRate(
                    detailBo.getRequestSuccess() - detailBo.getBizSuccessCode(),
                    detailBo.getRequestSuccess()
            );

            if (percentRate >= Double.parseDouble(alertInfo.getValue())) {
                buildAlertString(sb, detailBo);
                notGoodCount++;
            }
        }
        log.info("总测试链路 {},告警链路数:{}", detailBoList.size(), notGoodCount);
        sb
                .append("\n\n#### [点击跳转Titan报告详情]")
                .append("(")
                .append(systemProperties.insideReportUrlPrefix)
                .append(reportId).append(")")
                .append("\n#### 报告时间:").append(DateU.getCurrentTime());

        String htmlContent = MarkdownUtils.renderToHtml(sb.toString());
        String fileName = HTML_REPORT_PREFIX + LocalDateU.getFileTime(System.currentTimeMillis()) + HTML_SUFFIX;
        String path = systemProperties.outputPathPrefix + fileName;
        boolean status = writeToFile(path, htmlContent);
        log.info("链路监控指标告警,告警报告 {}, path:{}, 存储状态:{}", fileName, path, status);

        StringBuilder newSb = new StringBuilder();

        String detailUrl = systemProperties.htmlReportUrl + fileName;

        newSb
                .append("环境 [").append(CommonU.getConfigEnv().toUpperCase()).append("] ")
                .append(" Titan链路监控告警 \n")
                .append("场景:[")
                .append(alertReportBo.getSceneName())
                .append("] 监控异常\n")
                .append("总测试链路:")
                .append(detailBoList.size())
                .append(",告警链路数:")
                .append(notGoodCount)
                .append(".\n")
                .append("详情链接: ")
                .append(detailUrl)
                .append("\n");

        CacheManager.put(detailUrl, System.currentTimeMillis() + "");

        String toSend = newSb.toString();

        log.info("链路监控告警信息:{}", toSend);
        if (notGoodCount > 0) {
            //发给业务方
            String hook = alertInfo.getKey();
            MessageSender.send(hook, hook, toSend, null);
        }
        //发给维护者
        alarmService.send(MessageSender.Type.MAINTAINER, toSend);
    }

    private void buildAlertString(StringBuilder sb, LinkStatisticsDTO detailBo) {
        sb
                .append("\n").append(detailBo.getLinkId()).append(".链路:").append(detailBo.getLinkUrl()).append("\n")
                .append("- 总请求:").append(detailBo.getRequestTotal()).append(",")
                .append("http成功:").append(detailBo.getRequestSuccess()).append(",")
                .append("业务成功:").append(detailBo.getBizSuccessCode()).append(",")
                .append("业务失败:").append(detailBo.getBizErrorCode() + detailBo.getBizOtherCode()).append(".")
                .append("\n")


        ;
        List<String> retContents = detailBo.getRetContents();
        log.info("Return wrong contents :{}", retContents);
        if (retContents != null) {
            for (String content : retContents) {
                sb
                        .append("\n```")
                        .append(content)
                        .append("```\n");

            }
        }
        sb
                .append("<font color='red' style='font-weight: bold'> 结果不合格,请检查 </font>\n");

    }
}
