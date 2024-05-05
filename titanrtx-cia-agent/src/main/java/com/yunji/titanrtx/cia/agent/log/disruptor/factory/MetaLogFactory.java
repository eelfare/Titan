package com.yunji.titanrtx.cia.agent.log.disruptor.factory;

import com.lmax.disruptor.EventFactory;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.MateLog;

public class MetaLogFactory implements EventFactory<MateLog> {

    @Override
    public MateLog newInstance() {
        return new MateLog();
    }

}
