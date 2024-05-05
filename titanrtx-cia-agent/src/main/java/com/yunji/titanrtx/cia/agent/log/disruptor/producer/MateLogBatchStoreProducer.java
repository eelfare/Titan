package com.yunji.titanrtx.cia.agent.log.disruptor.producer;

import com.google.common.collect.Queues;
import com.yunji.titanrtx.cia.agent.log.Material;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.MateLog;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrxt.plugin.influxdb.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.yunji.titanrtx.cia.agent.log.core.Constants.*;

/**
 * 批量插入到 Influxdb 实现
 */
@Slf4j
public class MateLogBatchStoreProducer implements Material<MateLog> {

    private final BlockingQueue<Point> pointBlockingQueue = new LinkedBlockingQueue<>(INFLUXDB_POINT_QUEUE_SIZE);

    private StoreService storeService;

    private String rpName;


    private ExecutorService pullDataThreadPool = Executors.newFixedThreadPool(5);

    public MateLogBatchStoreProducer(StoreService storeService, String rpName) {
        this.storeService = storeService;
        this.rpName = rpName;
        initStoreThread();
    }

    private void initStoreThread() {
        for (int i = 0; i < 5; i++) {
            pullDataThreadPool.execute(new PointRunnable());
        }
    }

    @Override
    public void push(MateLog mateLog) {
        log.debug("MateLogStoreProducer:{}......................................", mateLog);
        Point point = Point.measurement(GlobalConstants.TOP_LINK_MATE_MEASUREMENT_NAME)
                .time(mateLog.getTime(), TimeUnit.NANOSECONDS)
                .addField("requestTimes", mateLog.getRequestTimes())
                .addField("successTimes", mateLog.getSuccessTimes())
                .addField("elapsed", mateLog.getElapsed())
                .tag("domain", mateLog.getDomain())
                .tag("path", mateLog.getPath())
                .build();

        try {
            pointBlockingQueue.put(point);
        } catch (InterruptedException e) {
            log.error("上报数据插入db异常", e);
        }
//        storeService.write(GlobalConstants.TOP_LINK_MATE_DB_NAME, rpName, point);
    }

    class PointRunnable implements Runnable {
        @Override
        public void run() {
            storeService.enableBatch(INFLUX_ENABLE_BATCH_SIZE, INFLUX_WRITE_FLUSH_DURATION, TimeUnit.MILLISECONDS);
            while (true) {
                List<Point> pointList = new ArrayList<>(INFLUX_ENABLE_BATCH_SIZE);
                try {
                    Queues.drain(pointBlockingQueue, pointList, INFLUX_ENABLE_BATCH_SIZE, POINT_QUEUE_DRAIN_TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    log.error("PointBlockingQueue 拉取数据异常", e);
                }
                //插入es
                if (pointList.size() > 0) {
                    try {
                        BatchPoints batchPoints = BatchPoints
                                .database(GlobalConstants.TOP_LINK_MATE_DB_NAME)
                                .retentionPolicy(rpName)
                                .build();

                        for (Point point : pointList) {
                            batchPoints.point(point);
                        }
                        storeService.batchWrite(batchPoints);
                    } catch (Exception e) {
                        log.error("批量插入 Points数据到 influxdb 异常", e);
                    }
                }
            }
        }
    }
}
