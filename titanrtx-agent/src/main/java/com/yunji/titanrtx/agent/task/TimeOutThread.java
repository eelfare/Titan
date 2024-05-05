package com.yunji.titanrtx.agent.task;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeOutThread extends Thread {

    private long timeout;

    private Execute execute;

    public TimeOutThread(long timeout, Execute execute) {
        this.timeout = timeout;
        this.execute = execute;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(timeout * 1000);
        } catch (InterruptedException e) {
            log.error("中断超时线程.......................................");
        }
        execute.doStop();
    }
}
