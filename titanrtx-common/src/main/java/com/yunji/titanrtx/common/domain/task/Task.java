package com.yunji.titanrtx.common.domain.task;

import com.yunji.titanrtx.common.enums.*;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Task implements Serializable {

    protected Integer id;

    protected String taskNo;

    protected TaskType taskType;

    protected long singleMachineConcurrent;

    protected long concurrent;

    protected long total;

    protected long timeout;

    private Strategy strategy;

    protected Flow flow;

    protected long throughPut;

    protected Sequence sequence;

    private List<Bullet> bullets;

    private Date startTime;

    /**
     * 参数 fetch 模式.
     */
    private ParamTransmit paramTransmit;


    private String alertHook;
}
