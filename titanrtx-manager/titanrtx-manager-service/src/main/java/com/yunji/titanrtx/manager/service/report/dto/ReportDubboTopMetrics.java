package com.yunji.titanrtx.manager.service.report.dto;

import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 自动化压测报告,Dubbo Top 耗时 指标
 *
 * @author leihuazhe
 * @since 2020.4.28
 */
@Data
public class ReportDubboTopMetrics {
    /**
     * 1588068930000
     */
    private long time;
    /**
     * abc
     */
    private String owner;
    /**
     * itembiz-provider
     */
    private String app;
    /**
     * com.yunji.scs.productcenter.api.openProduct.IProductAttributeService
     */
    private String service;
    /**
     * findAttributeListByItemIdOrSpuCode
     */
    private String method;
    /**
     * 72.1348,
     */
    private Double avgElapsed;
    /**
     * 2001.0,
     */
    private Double success;
    /**
     * 用来排序的,有大到小,降序.. 146343.0
     */
    private Double softKey;

    public static List<ReportDubboTopMetrics> build(InfluxStructures.Metric detailMetric) throws Exception {
        List<ReportDubboTopMetrics> dubboTopCostMetrics = new ArrayList<>();

        List<String> columns = detailMetric.getColumns();
        List<List<Object>> values = detailMetric.getValues();

        for (List<Object> value : values) {
            Map<String, Object> kvs = IntStream.range(0, columns.size()).boxed()
                    .collect(Collectors.toMap(columns::get, value::get));

            ReportDubboTopMetrics metric = new ReportDubboTopMetrics();

            BeanUtils.populate(metric, kvs);

            dubboTopCostMetrics.add(metric);
        }


        return dubboTopCostMetrics;
    }

}
