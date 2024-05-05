package com.yunji.titanrtx.agent.future;

import com.yunji.titanrtx.common.domain.statistics.StatisticsDetail;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.concurrent.CountDownLatch;

public interface CallBack<T,R> {

    void completed(R r);

    void cancelled();

    void failed(Exception ex);

    void start();

    void init(CountDownLatch countDownLatch, StatisticsDetail statisticsDetail, GenericObjectPool<T> pool, String paths);

    void returnObject();
}
