package com.yunji.titanrtx.cia.agent.log.disruptor.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MateLog extends Log {

    private long requestTimes;

    private long successTimes;

    private double elapsed;

}
