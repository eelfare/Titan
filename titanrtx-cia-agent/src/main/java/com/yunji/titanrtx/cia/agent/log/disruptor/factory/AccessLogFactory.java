package com.yunji.titanrtx.cia.agent.log.disruptor.factory;

import com.lmax.disruptor.EventFactory;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.AccessLog;

public class AccessLogFactory implements EventFactory<AccessLog> {

    @Override
    public AccessLog newInstance() {
        return new AccessLog();
    }

}
