package com.yunji.titanrtx.agent;

import java.util.concurrent.CountDownLatch;

/**
 * TODO
 *
 * @author leihz
 * @since 2020-05-15 10:09 上午
 */
public class CountDownLatchTest {

    public static void main(String[] args) {
        long st = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(21_3406_6392);

        while (countDownLatch.getCount() > 0) {
            {
                countDownLatch.countDown();
            }
        }

        System.out.println("cost:" + (System.currentTimeMillis() - st) + " ms");
    }
}
