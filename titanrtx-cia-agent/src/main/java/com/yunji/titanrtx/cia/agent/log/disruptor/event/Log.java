package com.yunji.titanrtx.cia.agent.log.disruptor.event;

import lombok.Data;

@Data
public class Log {

    protected String domain;

    protected String path;

    protected long time;

}
