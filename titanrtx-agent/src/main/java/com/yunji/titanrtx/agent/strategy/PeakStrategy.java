package com.yunji.titanrtx.agent.strategy;

import com.google.common.util.concurrent.RateLimiter;

public class PeakStrategy implements Strategy {

    private long startTime = System.currentTimeMillis();

    private static final int PEAK_TIMES = 10;

    private long conversionTime;

    private volatile boolean conversionFlag = true;

    private RateLimiter rateLimiter;

    private long concurrent;


    PeakStrategy(long concurrent, long total) {
        this.concurrent = concurrent;

        rateLimiter = RateLimiter.create(concurrent);

        long requestDuration = total / concurrent;
        requestDuration = (requestDuration + requestDuration /2) * 1000;
        conversionTime = requestDuration/PEAK_TIMES * 2;
    }


    @Override
    public RateLimiter doStrategy() {
        long endTime = System.currentTimeMillis();
        if ((endTime - startTime) > conversionTime){
            if (conversionFlag){
                rateLimiter = RateLimiter.create(1);
                conversionFlag = false;
            }else{
                rateLimiter = RateLimiter.create(concurrent);
                conversionFlag = true;
            }
            startTime =endTime;
        }
        return rateLimiter;
    }
}
