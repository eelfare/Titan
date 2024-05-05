package com.yunji.titanrtx.cia.agent.log.core;

import com.yunji.titanrtx.cia.agent.annotation.StreamAnnotation;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.AccessLog;
import com.yunji.titanrtx.common.domain.cia.Rules;
import com.yunji.titanrtx.common.enums.RulesType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@StreamAnnotation(index = 0)
public class FilterHandler extends AbstractStream{


    @Override
    protected RulesType rulesType() {
        return RulesType.FILTER;
    }


    @Override
    protected AccessLog doEvent(Rules rules, AccessLog accessLog) {
        log.debug("清洗掉数据:{}......................................",accessLog);
        return null;
    }

}
