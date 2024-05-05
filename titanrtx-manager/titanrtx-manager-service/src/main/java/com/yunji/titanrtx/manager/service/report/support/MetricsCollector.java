package com.yunji.titanrtx.manager.service.report.support;

import com.yunji.titanrtx.common.domain.statistics.*;
import com.yunji.titanrtx.manager.service.report.dto.*;
import com.yunji.titanrtx.common.u.*;

import com.yunji.titanrtx.manager.service.common.SystemProperties;
import com.yunji.titanrtx.plugin.http.AHCClientTool;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.yunji.titanrtx.manager.service.report.support.ReportUtils.GSON;

/**
 * 外部数据报告收集
 *
 * @author leihuazhe
 * @since 2020.4.28
 */
@Slf4j
@Component
public class MetricsCollector {
    @Resource
    private SystemProperties systemProperties;

    private static ExecutorService executorService =
            Executors.newFixedThreadPool(20, new NamedThreadFactory("Outside-Pool"));


    /**
     * 根据请求url查询 dubbo 接口 top 耗时
     */
    private static final String TOP300_DUBBO_SEARCH_PREFIX =
            "select owner,app,service,method,avgElapsed,success,top(softKey,100) as softKey from \"1h\".top_data where ";
    /***
     * 根据请求url 查询 cgi 数据. IDC
     */
    private static final String CGI_TOP_SEARCH_BY_SERVICE_PREFIX =
            "select sum(success) as totalSuccess,sum(failure) as totalFailure,max(maxConcurrent) as maxConcurrent ," +
                    "max(maxElapsed) as maxElapsed ,mean(avgElapsed) as avgElapsed ,sum(softKey) as softKey from cgi_data ";


    //1.CGI
    //2.TOP300
    //3.异常告警

    /**
     * http://influxdb-tx.yunjiweidian.com/query?u=admin&p=admin&db=sentinel&q=
     * select sum(success) as success,sum(failure) as failure,mean(avgElapsed) as rt from cgi_data
     * where service='/yunjiitemapp/app/anon/getSelectionSecond.json'
     * and time >= 1588075453827ms and time <= 1588075553827ms
     * group by time(1m)#default#cgi#cgi_data
     * <p>
     * <p>
     * <p>
     * sum(success) as totalSuccess,sum(failure) as "失败",max(maxConcurrent) as "最大并行数",max(maxElapsed) as "最大耗时",mean(avgElapsed) as "平均耗时" ,sum(softKey) as softKey
     *
     * @return
     */
    public List<ReportCGIMetrics> collectLinkCGIMetric(OutsideStatistics outsideStatistics) {
        Statistics statistics = outsideStatistics.getStatistics();

        List<CompletableFuture<ReportCGIMetrics>> linkedMetricFutures = new ArrayList<>();

        Map<Integer, String> urlIdMap = statistics.getUrlIdMap();

        for (Map.Entry<Integer, StatisticsDetail> entry : statistics.getDetailMap().entrySet()) {
            Integer id = entry.getKey();
            StatisticsDetail statisticsDetail = entry.getValue();
//            log.info("Statistics entry -> key:{},statisticsDetail:{}", id, statisticsDetail);

            // 只对 http请求进行cgi数据获取
            if (CollectionU.isNotEmpty(urlIdMap) && StringUtils.isNotEmpty(urlIdMap.get(id))) {
                String url = urlIdMap.get(id);
                CompletableFuture<ReportCGIMetrics> linkMetricFuture = CompletableFuture.supplyAsync(() -> {
                    return collectCGIMetricByLinkService(url, outsideStatistics.getStartTime(), outsideStatistics.getEndTime());
                }, executorService);

                linkedMetricFutures.add(linkMetricFuture);
            }
        }
        //List<Future<StatisticsBo>> -> Future<List<StatisticsBo>>
        CompletableFuture<List<ReportCGIMetrics>> listCompletableFuture = CompletableFuture
                .allOf(linkedMetricFutures.toArray(new CompletableFuture[0]))
                .thenApply(value -> linkedMetricFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        List<ReportCGIMetrics> cgiLinkMetrics = new ArrayList<>();
        try {
            cgiLinkMetrics = listCompletableFuture.get();
        } catch (Exception e) {
            log.error("Got CGI error,cause: " + e.getMessage(), e);
        }

        return cgiLinkMetrics;
    }

    public static void main(String[] args) {
        OutsideStatistics outsideStatistics = new OutsideStatistics();
        outsideStatistics.setStartTime(System.currentTimeMillis() - 120 * 1000);
        outsideStatistics.setEndTime(System.currentTimeMillis());
        MetricsCollector collector = new MetricsCollector();
        List<ReportDubboTopMetrics> dubboTopCostMetrics = collector.collectTopDubboCostMetric(outsideStatistics);

        System.out.println(dubboTopCostMetrics);

    }


    /**
     * https://grafana-tx.yunjiweidian.com/d/hdaIROZik/dubbohao-shi-pai-xing?orgId=1
     * 1.Dubbo top 接口耗时排行查询, 并按从大到小排序
     */
    public List<ReportDubboTopMetrics> collectTopDubboCostMetric(OutsideStatistics statistics) {
        String params = "u=admin&p=admin&db=sentinel&epoch=ms&q=" +
                TOP300_DUBBO_SEARCH_PREFIX +
                "time > " +
                statistics.getStartTime() +
                "ms and time < " +
                statistics.getEndTime() +
                "ms and avgElapsed>20#single#dubbo#top_data#dubbo_top_data&epoch=ms";

//        log.info("influx db request url:{}, params: {}", systemProperties.influxdbUrl, params);

        InfluxStructures.Root root = requestFor(systemProperties.influxdbUrl, params);

        if (root != null) {
            try {
                List<InfluxStructures.Metric> metrics = root.getResults().get(0).getSeries();
                List<ReportDubboTopMetrics> topCostMetrics = ReportDubboTopMetrics.build(metrics.get(0));
                topCostMetrics.sort((o1, o2) -> (int) (o2.getSoftKey() - o1.getSoftKey()));

                return topCostMetrics;
            } catch (Exception e) {
                log.error("Build top300 metric bean got error: " + e.getMessage());
            }
        }

        return new ArrayList<>();
    }


    /**
     * @param service resource -> url path.
     * @param st      start time
     * @param et      end time
     * @return
     */
    private ReportCGIMetrics collectCGIMetricByLinkService(String service, long st, long et) {
        String params = "u=admin&p=admin&db=sentinel&q=" +
                CGI_TOP_SEARCH_BY_SERVICE_PREFIX +
                "where service='" + service + "'" +
                " and " +
                "time >=" + st + "ms " +
                " and " +
                "time <=" + et + "ms " +
                "group by time(1m)#default#cgi#cgi_data";

        LogU.info("Influx reequest url:{}, params: {}", systemProperties.influxdbUrl, params);

        InfluxStructures.Root root = requestFor(systemProperties.influxdbUrl, params);
        List<ReportCGIMetrics> cgiLinkMetrics = null;
        try {
            cgiLinkMetrics = ReportCGIMetrics.build(service, root.getResults().get(0).getSeries().get(0));
            return ReportCGIMetrics.sum(cgiLinkMetrics);
        } catch (Exception e) {
            //todo 单个错误不能影响全局.
            LogU.error("[Influx return error] link:{} request failed:{}", service, e.getMessage());
            return ReportCGIMetrics.empty(service);
        }
    }


    /**
     * 带重试模式的请求 influxdb 获取数据.
     */
    protected InfluxStructures.Root requestFor(String url, String params) {
        int requestCount = 1;
        do {
            try {
                String resultJson = AHCClientTool.doGet(url, params, "", "application/json", "utf-8");
//                log.info("resultJson:[ {} ]", resultJson);
                return GSON.fromJson(resultJson, InfluxStructures.Root.class);
            } catch (Exception e) {
                log.error("Request [" + requestCount + "] times for influxDB got error, url:[" + url + "], cause: " + e.getMessage());
            }
        } while (requestCount++ < 3);
        log.error("Try 3 times request for influxDB failed,please check.");
        return null;
    }

}
