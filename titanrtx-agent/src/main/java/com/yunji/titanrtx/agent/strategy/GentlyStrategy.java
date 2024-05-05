package com.yunji.titanrtx.agent.strategy;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GentlyStrategy implements Strategy {

    private long startTime = System.currentTimeMillis();

    private static final int GENTLY_STRATEGY_CONVERSION_TIME = 1000;

    private long step = 0 ;

    private long limitConcurrent = 1;

    private RateLimiter rateLimiter;

    private long concurrent;

    GentlyStrategy(long concurrent, long total) {
        this.concurrent = concurrent;
        rateLimiter = RateLimiter.create(limitConcurrent);
        /*
         *  F(x)=concurrent
         *  0< x < concurrent
         *  F(times) = total
         *  F(times) = F(x) * times
         */
        long requestDuration = total / concurrent;
        step = (total - requestDuration) / ( requestDuration / 2 * (requestDuration - 1));
        step = step == 0 ? 1 :step;
        log.info("requestTotal:{},requestDuration:{},step:{}...............................................",total,requestDuration,step);
    }


    @Override
    public RateLimiter doStrategy() {
        long endTime = System.currentTimeMillis();
        if ((endTime - startTime) > GENTLY_STRATEGY_CONVERSION_TIME && limitConcurrent < concurrent){
            limitConcurrent = limitConcurrent + step;
            if (limitConcurrent > concurrent){
                limitConcurrent = concurrent;
            }
            rateLimiter = RateLimiter.create(limitConcurrent);
            startTime = endTime;
        }
        return rateLimiter;
    }

}
