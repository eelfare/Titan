package com.yunji.titanrtx.agent.collect.report;

import com.yunji.titanrtx.agent.collect.Collector;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrxt.plugin.influxdb.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-10-29 14:31
 * @Version 1.0
 */
@Slf4j
public class QpsCollectServiceImpl implements ReportCollectService {
    StoreService storeService;

    String rpName;

    boolean reportQps;

    // 上报时间
    private int agentReportInterval;

    public QpsCollectServiceImpl(boolean reportQps, StoreService storeService, String rpName, int agentReportInterval) {
        this.storeService = storeService;
        this.rpName = rpName;
        this.agentReportInterval = agentReportInterval;
        this.reportQps = reportQps;
    }

    @Override
    public void report(boolean isOver, String agentIp) {
        if (!reportQps) {
            // 清除数据
            Collector.requestConcurrent.clear();
            Collector.responseConcurrent.clear();
            return;
        }
        // 异步批量上传
        BatchPoints batchPoints = BatchPoints.database(GlobalConstants.AGENTQPS_COLLECT_QPS_BD_NAME).build();

        NavigableSet<String> keys = Collector.requestConcurrent.keySet();
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        String delKey = keys.first();
        int count = 0;
        while (StringUtils.isNotEmpty(delKey) && (isOver || count < agentReportInterval)) {
            ConcurrentHashMap<String, AtomicReference<long[]>> atomicLinkIdQpsMap = Collector.responseConcurrent.get(delKey);

            ConcurrentHashMap<String, AtomicInteger> atomicLinkQpsHashMap = Collector.requestConcurrent.get(delKey);
//            log.info("获取相关的数据{}",atomicLinkQpsHashMap);
            Set<Map.Entry<String, AtomicInteger>> entries = atomicLinkQpsHashMap.entrySet();
            for (Map.Entry<String, AtomicInteger> temp : entries) {
                AtomicReference<long[]> atomicReference = atomicLinkIdQpsMap.get(temp.getKey());
                long[] longs = atomicReference.get();
                Long timeToSet = Long.valueOf(delKey);
                Point point = Point.measurement(GlobalConstants.AGENTQPS_COLLECT_QPS_MEASUREMENT_NAME)
                        .tag(GlobalConstants.INFLUX_TAG_AGENT_IP, agentIp)
                        .tag(GlobalConstants.INFLUX_TAG_PATH, temp.getKey())
                        .time(timeToSet, TimeUnit.SECONDS)
                        .addField(GlobalConstants.INFLUX_FILED_2XX, longs[0])
                        .addField(GlobalConstants.INFLUX_FILED_3XX, longs[1])
                        .addField(GlobalConstants.INFLUX_FILED_4XX, longs[2])
                        .addField(GlobalConstants.INFLUX_FILED_5XX, longs[3])
                        .addField(GlobalConstants.INFLUX_FILED_OTHER, longs[4])
                        .addField(GlobalConstants.INFLUX_FILED_EXPIRED, longs[5])
                        .addField(GlobalConstants.INFLUX_FILED_RECEIVED, longs[6])
                        .addField(GlobalConstants.INFLUX_FILED_ERROR, longs[7])
                        .addField(GlobalConstants.INFLUX_FILED_SEND, temp.getValue()).build();

                // 保存数据至influxdb中
                batchPoints.point(point);
            }
            Collector.clear(delKey); //
            count++;
            if (!Collector.requestConcurrent.isEmpty()) {
                delKey = Collector.requestConcurrent.keySet().first();
            } else {
                delKey = "";
            }
        }
        storeService.batchWrite(batchPoints);
    }
}
