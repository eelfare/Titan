package com.yunji.titanrtx.manager.web;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-11-22 09:47
 * @Version 1.0
 */
@Slf4j
public class LatchTest implements Runnable {
    public static AtomicInteger count = new AtomicInteger();
    public CountDownLatch countDownLatch;

    public LatchTest(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public static void main(String[] args){

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(100);
        Date start = new Date();
        for (int i = 0; i < 100; i++) {
            scheduledExecutorService.execute(new LatchTest(countDownLatch));
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("消耗时间:{}ms",new Date().getTime() - start.getTime());
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run(){
        log.info("总计：{}", count.incrementAndGet());
        countDownLatch.countDown();
    }
}
