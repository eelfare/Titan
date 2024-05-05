package com.yunji.titanrtx.cia.agent.log.disruptor.producer;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.yunji.titanrtx.cia.agent.log.Material;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.AccessLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessLogProducer implements Material<AccessLog> {

    private final RingBuffer<AccessLog> ringBuffer;

    public AccessLogProducer(RingBuffer<AccessLog> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private static final EventTranslatorOneArg<AccessLog, AccessLog> TRANSLATOR =
            (out, sequence, in) -> {
                out.setDomain(in.getDomain());
                out.setPath(in.getPath());
                out.setTime(in.getTime());
                out.setElapsed(in.getElapsed());
                out.setRespCode(in.getRespCode());
                out.setParam(in.getParam());
            };


    @Override
    public void push(AccessLog accessLog) {
        log.debug("AccessLogProducer:{}......................................",accessLog);
        ringBuffer.publishEvent(TRANSLATOR, accessLog);
    }
}
