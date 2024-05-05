package com.yunji.titanrtx.cia.agent.log.disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.yunji.titanrtx.common.u.NamedThreadFactory;

public class DisruptorMain<T> {

    private static final int BUFFER_SIZE = 1024;

    @SafeVarargs
    public final RingBuffer<T> init(EventFactory<T> factory, String threadName, WorkHandler<T>... workHandler){
        Disruptor<T> disruptor = new Disruptor<>(factory, BUFFER_SIZE,new NamedThreadFactory(threadName), ProducerType.MULTI, new SleepingWaitStrategy());
        disruptor.handleEventsWithWorkerPool(workHandler);
        disruptor.start();
        return disruptor.getRingBuffer();
    };

}
