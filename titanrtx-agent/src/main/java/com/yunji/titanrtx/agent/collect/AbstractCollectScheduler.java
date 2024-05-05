package com.yunji.titanrtx.agent.collect;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-10-29 10:31
 * @Version 1.0
 */
@Slf4j
public abstract class AbstractCollectScheduler implements CollectScheduler {
    ScheduledFuture<?> future = null;
    ScheduledExecutorService scheduledExecutorService = null;

    public AbstractCollectScheduler(int poolSize) {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(poolSize);
    }

    @Override
    public void stop() {
        log.info("停止定时任务");
        if (future != null) {
            future.cancel(true);
        }
    }

    @Override
    public void schedule(long initialDelay,
                         long delay,
                         TimeUnit unit) {
        future = scheduledExecutorService.scheduleWithFixedDelay(this, initialDelay, delay, unit);
    }
}
