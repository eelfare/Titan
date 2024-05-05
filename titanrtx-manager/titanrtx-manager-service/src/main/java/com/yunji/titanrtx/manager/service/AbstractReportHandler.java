package com.yunji.titanrtx.manager.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yunji.titanrtx.common.alarm.*;
import com.yunji.titanrtx.common.domain.statistics.*;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.task.Report;
import com.yunji.titanrtx.common.u.*;
import com.yunji.titanrtx.manager.dao.bos.*;
import com.yunji.titanrtx.manager.dao.bos.http.PairBo;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.service.http.HttpBaseLineService;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import com.yunji.titanrtx.manager.service.report.alert.AlertReportGenerator;
import com.yunji.titanrtx.manager.service.report.support.ReportUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractReportHandler implements Report, Summary {
    private static ExecutorService executorService = Executors.newFixedThreadPool(20, new NamedThreadFactory("influx-db-pool"));


    @Value("${yj.influxdb_url:http://influxdbrouting.dev1.sh.tx.yunjiweidian.com/query}")
    private String influxdbUrl;
    @Resource
    private HttpBaseLineService httpBaseLineService;

    @Value("${is.stress.evn:#{true}}")
    private boolean isStressEvn;

    @Resource
    private HttpSceneService httpSceneService;

    @Resource
    private AlertReportGenerator alertHandler;

    @Autowired
    private AlarmService alarmService;


    @Value("${rt.error:#{30}}")
    int rtError; // rt允许误差
    @Value("${tps.error:#{20}}")
    int tpsError; // tps允许误差


    protected SerializerFeature[] features = new SerializerFeature[]{SerializerFeature.WriteClassName};

    @Override
    public void report(Statistics statistics) {
        log.info(".....进入{},开始report指标数据 .....", getClass().getSimpleName());
        AtomicInteger emptyCGIDataCount = new AtomicInteger(); // CGI数据为空的计数器
        AtomicReference<String> improperLinkBaseLine = new AtomicReference<>(); // 非正常新能基线链路
        Integer sceneId = CommonU.parseTaskNoToId(statistics.getTaskNo());
        SummaryStatistics summary = summary(sceneId, statistics, emptyCGIDataCount, improperLinkBaseLine);
        BaseEntity sceneEntity = updateSceneStatus(sceneId);
        List<? extends BaseEntity> bulletEntity = selectBullet(sceneEntity);
        PressureReportBo reportBo = new PressureReportBo();
        reportBo.setSceneEntity(sceneEntity);
        reportBo.setBulletEntity(bulletEntity);
        reportBo.setSum(summary);

        summary.setExecutionDuration(statistics.getEndTime().getTime() - statistics.getStartTime().getTime());
        reportBo.setStartTime(statistics.getStartTime());
        reportBo.setEndTime(statistics.getEndTime());

        int reportId = doInsert(sceneId, reportBo);

        // 开启线程去发送消息到企业微信
        if (statistics.getTaskType() == TaskType.HTTP) {
            alertHandler.checkAndAlert(sceneId, reportId, reportBo, statistics);
        } else {
            log.info("TaskType not http,should not alert.");
        }

        if (ReportUtils.notIDC()) {
            log.info("非IDC环境,开启线程对性能基线告警.");
            new Thread(() -> {
                if (emptyCGIDataCount.get() == statistics.getDetailMap().size()) { // 表示所有的数据都没有获取到
                    if (summary.getExecutionDuration() < 5 * 60 * 1000) {
                        return;
                    }
                    String msgContent = "TaskNo: [" + statistics.getTaskNo() + "] 所有的压测链路CGI数据都没有获取到,请检查.";
                    log.info("发送企业微信内容如下：{}", msgContent);
                    alarmService.send(MessageSender.Type.PERF_BASELINE, msgContent);
                } else {
                    String temp = improperLinkBaseLine.get();
                    log.info("不合格的基线链路 {}", temp);
                    if (StringUtils.isNotEmpty(temp) && sceneEntity instanceof HttpSceneEntity) {
                        HttpSceneEntity httpSceneEntity = (HttpSceneEntity) sceneEntity;
                        String webhook = httpSceneEntity.getWebhook();
                        if (StringUtils.isNotEmpty(webhook)) {
                            String dealWithLinkMsgContent = getDealWithLinkMsgContent(temp.substring(0, temp.length() - 1), reportId);
                            log.info("发送企业微信内容如下：{}", dealWithLinkMsgContent);

                            //自定义
                            //todo
                            MessageSender.send(webhook, webhook, dealWithLinkMsgContent, null);
                        }
                    }
                }
            }).start();
        }
        WebSocketService.sendMessage("执行压测已完成，请查看压测报告");
        log.info(".....结束report指标数据 {}. .....", getClass().getSimpleName());
    }


    @Override
    public SummaryStatistics summary(int sceneId, Statistics statistics, AtomicInteger emptyCGIDataCount, AtomicReference<String> improperLinkBaseLine) {
        SummaryStatistics bo = new SummaryStatistics();
//        List<StatisticsBo> innerBos = new ArrayList<>();
        List<CompletableFuture<StatisticsBo>> innerBoFutures = new ArrayList<>();
        long totalDuration = 0;
        long successTimes = 0;
        long failTimes = 0;
        long businessSuccessTimes = 0;
        long businessFailTimes = 0;
        // 获取链路信息
        Map<Integer, String> urlIdMap = statistics.getUrlIdMap();
        for (Map.Entry<Integer, StatisticsDetail> entry : statistics.getDetailMap().entrySet()) {
//            log.info("entry is {}", entry.getValue());
            Integer id = entry.getKey();
            StatisticsDetail statisticsDetail = entry.getValue();

            totalDuration += statisticsDetail.getDuration();
            successTimes += statisticsDetail.successTimes();
            failTimes += statisticsDetail.failTimes();
            businessSuccessTimes += statisticsDetail.businessSuccessTime();
            businessFailTimes += statisticsDetail.businessFailTime();

            StatisticsBo innerBo = covertInnerBo(id, statisticsDetail);
            // 只对 http请求进行cgi数据获取
            if (CollectionU.isNotEmpty(urlIdMap) &&
                    StringUtils.isNotEmpty(urlIdMap.get(id)) &&
                    //IDC环境不进行性能基线
                    !ReportUtils.isIDC()
            ) {
                String url = urlIdMap.get(id);
                Date startTime = statistics.getStartTime();
                Date endTime = statistics.getEndTime();
                long start = startTime.getTime();
                long end = endTime.getTime();
                if ((end - start) > 10 * 60 * 1000) { // 掐头去尾 一分钟
                    start = start + 60 * 1000;
                    end = end - 60 * 1000;
                }
                // 线程池执行CGI远程访问
                long finalStart = start;
                long finalEnd = end;
                CompletableFuture<StatisticsBo> innerBoFuture = CompletableFuture.supplyAsync(() -> new RemoteCgiThread(httpBaseLineService, emptyCGIDataCount,
                                improperLinkBaseLine, innerBo, sceneId, url, finalStart, finalEnd, influxdbUrl, rtError, tpsError)
                                .process(),
                        executorService
                );
                innerBoFutures.add(innerBoFuture);
            } else {
                innerBoFutures.add(CompletableFuture.completedFuture(innerBo));
            }
        }
        //List<Future<StatisticsBo>> -> Future<List<StatisticsBo>>
        CompletableFuture<List<StatisticsBo>> listCompletableFuture = CompletableFuture
                .allOf(innerBoFutures.toArray(new CompletableFuture[0]))
                .thenApply(value -> innerBoFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        List<StatisticsBo> innerBos = new ArrayList<>();
        try {
            innerBos = listCompletableFuture.get();
        } catch (Exception e) {
            log.error("Got CGI error,cause: " + e.getMessage(), e);
        }

        Collections.sort(innerBos, new Comparator<StatisticsBo>() {
            @Override
            public int compare(StatisticsBo o1, StatisticsBo o2) {
                Integer i1 = parseToInt(o1.getRequestSuccessCodeRate().replace("%", ""));
                Integer i2 = parseToInt(o2.getRequestSuccessCodeRate().replace("%", ""));
                return i1 - i2;
            }
        });
        bo.setBos(innerBos);
        bo.setRequestTotal(statistics.getRequestTotal());
        bo.setTotalDuration(totalDuration);
        bo.setAverageDuration(CommonU.divide(bo.getTotalDuration(), bo.getRequestTotal(), 2, RoundingMode.HALF_UP));

        if (bo.getAverageDuration() != 0) {
            bo.setQps((int) (1000 / bo.getAverageDuration()));
        }

        bo.setRequestSuccessCode(successTimes);
        bo.setRequestFailCode(failTimes);

        bo.setRequestSuccessCodeRate(CommonU.divideRate(bo.getRequestSuccessCode(), statistics.getRequestTotal(), 3));
        bo.setRequestFailCodeRate(CommonU.divideRate(bo.getRequestFailCode(), statistics.getRequestTotal(), 3));

        bo.setBusinessSuccessCode(businessSuccessTimes);
        bo.setBusinessFailCode(businessFailTimes);
        bo.setBusinessSuccessCodeRate(CommonU.divideRate(bo.getBusinessSuccessCode(), businessSuccessTimes + businessFailTimes, 3));
        bo.setBusinessFailCodeRate(CommonU.divideRate(bo.getBusinessFailCode(), businessSuccessTimes + businessFailTimes, 3));

        LogU.info("汇总压测结果,summary：{}......................................\n\n statistics:{}.......", bo, statistics);
        return bo;
    }

    // link的消息体
    private String getDealWithLinkMsgContent(String links, int reportId) {
        @Data
        class Markdown {
            String content;
        }

        @Data
        class Msg {
            String msgtype;
            Markdown markdown;
        }
        Msg msg = new Msg();
        Markdown markdown = new Markdown();

        StringBuilder tips = new StringBuilder();
        tips.append("#### 压测报告汇总")
                .append("\n")
                .append("##### 出现部分接口性能基线不合格：")
                .append("\n");
        AtomicInteger total = new AtomicInteger(0);
        String[] split = links.split(",");
        for (String link : split) {
            tips.append(total.incrementAndGet()).append(". ").append(link).append("\n");
            if (total.get() == 3) {
                tips.append(total.incrementAndGet()).append(". ").append("...").append("\n");
                break;
            }
        }
        tips.append("\n---").append("\n")
                .append("#### [查看详情](")
                .append(isStressEvn ? "http://txtitanx.yunjiweidian.com/" : "https://titanx-tx.yunjiweidian.com/")
                .append("/view/main.html#/httpReportDetail?id=")
                .append(reportId).append(")");

        markdown.setContent(tips.toString());
        msg.setMsgtype("markdown");
        msg.setMarkdown(markdown);
        return JSONObject.toJSONString(msg);
    }




    private StatisticsBo covertInnerBo(Integer id, StatisticsDetail statisticsDetail) {

        StatisticsBo bo = new StatisticsBo();
        bo.setId(id);
        bo.setRequestSuccessCode(statisticsDetail.successTimes());
        bo.setRequestFailCode(statisticsDetail.failTimes());
        long requestTotal = statisticsDetail.requestTimes();

        bo.setRequestSuccessCodeRate(CommonU.divideRate(bo.getRequestSuccessCode(), requestTotal, 3));
        bo.setRequestFailCodeRate(CommonU.divideRate(bo.getRequestFailCode(), requestTotal, 3));

        bo.setDuration(statisticsDetail.getDuration());
        bo.setRequestTotal(requestTotal);

        bo.setAverageDuration(CommonU.divide(bo.getDuration(), bo.getRequestTotal(), 2, RoundingMode.HALF_UP));
        if (bo.getAverageDuration() != 0) {
            bo.setQps((int) (1000 / bo.getAverageDuration()));
        }
        List<PairBo> pairBos = convertPairBo(requestTotal, statisticsDetail.getBUSINESS_CODE_MAP());
        bo.setPairBos(pairBos);

        return bo;
    }

    private List<PairBo> convertPairBo(long requestTotal, Map<Integer, AtomicInteger> codeMap) {
        List<PairBo> bos = new ArrayList<>();

        codeMap.forEach((integer, atomicInteger) -> {
            PairBo bo = new PairBo();
            bo.setCode(integer);
            bo.setTimes(atomicInteger.intValue());
            bo.setCodeRate(CommonU.divideRate(atomicInteger.intValue(), requestTotal, 3));
            bos.add(bo);
        });

        return bos;
    }

    private Integer parseToInt(String nums) {
        return new BigDecimal(nums)
                .setScale(0, BigDecimal.ROUND_HALF_UP)
                .intValue();
    }


    protected abstract int doInsert(Integer sceneId, PressureReportBo reportBo);

    protected abstract List<? extends BaseEntity> selectBullet(BaseEntity baseEntity);

    protected abstract BaseEntity updateSceneStatus(Integer sceneId);


}
