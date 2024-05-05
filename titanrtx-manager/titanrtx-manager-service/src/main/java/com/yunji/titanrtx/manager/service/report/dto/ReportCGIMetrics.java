package com.yunji.titanrtx.manager.service.report.dto;

import com.yunji.titanrtx.common.u.CommonU;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 自动化压测报告,CGI 指标
 *
 * @author leihuazhe
 * @since 2020.4.29
 */
@Data
public class ReportCGIMetrics {

    private String service = "NULL";
    /**
     * 1588068930000
     */
    private long time;
    /**
     * 8.0
     */
    private Double totalSuccess;
    /**
     * 0.0
     */
    private Double totalFailure;
    /**
     * 1.0
     */
    private Double maxConcurrent;
    /**
     * 最大耗时
     */
    private Double maxElapsed;
    /**
     * 平均耗时
     */
    private Double avgElapsed;
    /**
     * 用来排序的,有大到小,降序.. 146343.0
     */
    private Double softKey;


    public static List<ReportCGIMetrics> build(String service, InfluxStructures.Metric detailMetric) throws Exception {
        List<ReportCGIMetrics> cgiPerLinkMetrics = new ArrayList<>();

        List<String> columns = detailMetric.getColumns();
        List<List<Object>> values = detailMetric.getValues();

        for (List<Object> value : values) {
            Map<String, Object> kvs = IntStream.range(0, columns.size()).boxed()
                    .collect(Collectors.toMap(columns::get, idx -> {
                        Object obj = value.get(idx);
                        if (obj == null) {
                            if (columns.get(idx).equals("time"))
                                return -1L;
                            return -1D;
                        } else return obj;
                    }));

            ReportCGIMetrics metric = new ReportCGIMetrics();

            BeanUtils.populate(metric, kvs);

            metric.setService(service);

            cgiPerLinkMetrics.add(metric);
        }

        return cgiPerLinkMetrics;
    }

    public static ReportCGIMetrics sum(List<ReportCGIMetrics> linkMetricList) {
        //totalSuccess 累加
        double totalSuccess = 0D;
        //totalFailure 累加
        double totalFailure = 0D;

        //maxConcurrent 取最大值
        double maxConcurrent = 0D;
        //maxElapsed 取最大值
        double maxElapsed = 0D;

        //avgElapsed 求平均
        double avgElapsed0 = 0D;

        int avgCount = 0;
        String service = "NULL";
        for (ReportCGIMetrics linkMetric : linkMetricList) {
            if (linkMetric.getSoftKey() == null || linkMetric.getSoftKey() == -1D) continue;
            totalSuccess += linkMetric.totalSuccess;
            totalFailure += linkMetric.totalFailure;

            maxConcurrent = Math.max(maxConcurrent, linkMetric.maxConcurrent);
            maxElapsed = Math.max(maxElapsed, linkMetric.maxElapsed);

            avgElapsed0 += linkMetric.avgElapsed;
            avgCount++;

            service = linkMetric.service;
        }
        double avgElapsed = 0D;
        if (avgCount > 0) {
            avgElapsed = CommonU.divide(avgElapsed0, avgCount, 2, RoundingMode.HALF_UP);
        }
        ReportCGIMetrics newMetric = new ReportCGIMetrics();

        newMetric.setTotalSuccess(totalSuccess);
        newMetric.setTotalFailure(totalFailure);
        newMetric.setMaxConcurrent(maxConcurrent);
        newMetric.setMaxElapsed(maxElapsed);
        newMetric.setAvgElapsed(avgElapsed);

        newMetric.setService(service);

        return newMetric;
    }


    public static ReportCGIMetrics empty(String service) {
        ReportCGIMetrics cgiLinkMetric = new ReportCGIMetrics();
        cgiLinkMetric.setService(service);
        cgiLinkMetric.setAvgElapsed(-1D);
        cgiLinkMetric.setTotalSuccess(-1D);
        cgiLinkMetric.setTotalFailure(-1D);
        cgiLinkMetric.setMaxConcurrent(-1D);
        cgiLinkMetric.setMaxElapsed(-1D);
        return cgiLinkMetric;
    }
}
