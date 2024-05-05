package com.yunji.titanrtx.agent.strategy;

import com.google.common.util.concurrent.RateLimiter;

public class FixationStrategy implements Strategy {

    private RateLimiter rateLimiter;

    FixationStrategy(long concurrent, long total) {
        rateLimiter = RateLimiter.create(concurrent);
    }

    @Override
    public RateLimiter doStrategy() {
        return rateLimiter;
    }
}
