package com.yunji.titanrtx.cia.agent.log.disruptor.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ParamLog extends Log{

    private String param;

    private long respCode;

    private double elapsed;

}
