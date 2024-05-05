package com.yunji.titanrtx.cia.agent.log.disruptor.factory;

import com.lmax.disruptor.EventFactory;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.ParamLog;

public class ParamLogFactory implements EventFactory<ParamLog> {

    @Override
    public ParamLog newInstance() {
        return new ParamLog();
    }

}
