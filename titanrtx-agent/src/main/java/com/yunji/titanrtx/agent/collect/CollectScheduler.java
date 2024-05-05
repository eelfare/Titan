package com.yunji.titanrtx.agent.collect;

import java.util.concurrent.TimeUnit;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-10-29 10:13
 * @Version 1.0
 */
public interface CollectScheduler extends Runnable {

    void start();

    void stop();

    void schedule(long initialDelay,
                          long delay,
                          TimeUnit unit);
}
