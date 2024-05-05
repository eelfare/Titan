package com.yunji.titanrtx.common.domain.progress;

import lombok.Data;

import java.io.Serializable;

@Data
public class Progress implements Serializable {

    private long total;

    private long requestTimes;

    private long waitResponseTimes;

    protected int memory;

    protected int cpu;

    protected int network;

}
