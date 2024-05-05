package com.yunji.titanrtx.cia.agent.log.disruptor.producer;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.yunji.titanrtx.cia.agent.log.Material;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.ParamLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParamLogProducer implements Material<ParamLog> {

    private final RingBuffer<ParamLog> ringBuffer;

    public ParamLogProducer(RingBuffer<ParamLog> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private static final EventTranslatorOneArg<ParamLog, ParamLog> TRANSLATOR =
            (out, sequence, in) -> {
                out.setDomain(in.getDomain());
                out.setParam(in.getParam());
                out.setPath(in.getPath());
                out.setTime(in.getTime());
                out.setRespCode(in.getRespCode());
                out.setElapsed(in.getElapsed());
            };


    @Override
    public void push(ParamLog paramLog) {
        log.debug("ParamLogProducer:{}......................................",paramLog);
        ringBuffer.publishEvent(TRANSLATOR,paramLog);
    }
}
