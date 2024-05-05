package com.yunji.titanrtx.agent.strategy;

import com.google.common.util.concurrent.RateLimiter;

public interface Strategy {

    RateLimiter doStrategy();

    static Strategy doSelect(com.yunji.titanrtx.common.enums.Strategy strategyType, long concurrent, long total){
        Strategy strategy;
        if (com.yunji.titanrtx.common.enums.Strategy.FIXATION == strategyType){
            strategy = new FixationStrategy(concurrent,total);
        }else if (com.yunji.titanrtx.common.enums.Strategy.GENTLY == strategyType){
            strategy = new GentlyStrategy(concurrent,total);
        }else{
            strategy = new PeakStrategy(concurrent,total);
        }
        return strategy;
    }

}
