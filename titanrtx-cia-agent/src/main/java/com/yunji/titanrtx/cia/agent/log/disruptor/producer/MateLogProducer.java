package com.yunji.titanrtx.cia.agent.log.disruptor.producer;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.yunji.titanrtx.cia.agent.log.Material;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.MateLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MateLogProducer implements Material<MateLog> {

    private final RingBuffer<MateLog> ringBuffer;

    public MateLogProducer(RingBuffer<MateLog> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private static final EventTranslatorOneArg<MateLog, MateLog> TRANSLATOR =
            (out, sequence, in) -> {
                out.setDomain(in.getDomain());
                out.setPath(in.getPath());
                out.setTime(in.getTime());
                out.setElapsed(in.getElapsed());
                out.setRequestTimes(in.getRequestTimes());
                out.setSuccessTimes(in.getSuccessTimes());
            };


    @Override
    public void push(MateLog mateLog) {
        log.debug("MateLogProducer:{}......................................",mateLog);
        ringBuffer.publishEvent(TRANSLATOR, mateLog);
    }
}
