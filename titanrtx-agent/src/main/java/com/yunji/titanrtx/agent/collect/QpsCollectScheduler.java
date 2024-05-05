package com.yunji.titanrtx.agent.collect;

import com.yunji.titanrtx.agent.boot.AgentRegister;
import com.yunji.titanrtx.agent.collect.report.ReportCollectService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-10-29 10:11
 * @Version 1.0
 */
@Slf4j
public class QpsCollectScheduler extends AbstractCollectScheduler {
    ReportCollectService qpsCollectService;

    /**
     * 上报的时间间隔（单位：秒）
     */
    private int reportInterval;

    /**
     * @param poolSize          线程池大小
     * @param qpsCollectService
     */
    public QpsCollectScheduler(int poolSize, int reportInterval, ReportCollectService qpsCollectService) {
        super(poolSize);
        this.qpsCollectService = qpsCollectService;
        this.reportInterval = reportInterval;
    }

    @Override
    public void start() {
        schedule(1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
//        log.info("采集次数:{}",Collector.requestConcurrent.size());
        if (Collector.requestConcurrent.size() == reportInterval + 1) {  // 上传前一秒的所有数据
//            log.info("上报前10秒采集的数据");
            report(false);
        }
    }

    /**
     * 是否是结束提交数据
     *
     * @param isOver
     */
    protected void report(boolean isOver) {
        qpsCollectService.report(isOver, AgentRegister.AGENT_META.getAddress());
    }

    @Override
    public void stop() {
        super.stop();
        report(true);
    }
}
