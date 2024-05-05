package com.yunji.titanrtx.cia.agent.log.disruptor.handler;

import com.lmax.disruptor.WorkHandler;
import com.yunji.titanrtx.cia.agent.log.Material;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.ParamLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParamLogHandler implements WorkHandler<ParamLog> {


    private Material<ParamLog> paramLogMaterial;

    public ParamLogHandler(Material<ParamLog> paramLogMaterial) {
        this.paramLogMaterial = paramLogMaterial;
    }

    @Override
    public void onEvent(ParamLog event) {
        log.debug("ParamLogHandler:{}......................................", event);
        try {
            paramLogMaterial.push(event);
        } catch (Exception e) {
            log.error(e.getMessage() + ", time: " + (event != null ? event.getTime() : "-1"));
        }
    }
}
