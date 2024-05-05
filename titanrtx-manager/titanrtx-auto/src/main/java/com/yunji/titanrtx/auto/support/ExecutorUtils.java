package com.yunji.titanrtx.auto.support;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ExecutorUtils
 *
 * @author leihz
 * @since 2020-06-19 4:50 下午
 */
@Slf4j
public class ExecutorUtils {

    private static ScheduledExecutorService scheduledExecutors = Executors.newScheduledThreadPool(10);


    public static void schedule(Runnable r, long time, TimeUnit timeUnit) {
        scheduledExecutors.schedule(r, time, timeUnit);
    }

}
