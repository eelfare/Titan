package com.yunji.titanrtx.manager.service.common.eventbus;

import com.yunji.titanrtx.common.enums.TaskType;
import lombok.Data;


@Data
public class ParamEvent {

    private int id;

    private Event event;

    private TaskType taskType;

    public enum Event {
        ADD,
        ADD_OR_UPDATE,
        DELETE,
        INSERT_LINK,
        /**
         * 流量构造导出事件.
         */
        BATCH_EXPORT,
        /**
         * 压测 preCheck异常.
         */
        STRESS_EVENT
    }

    public ParamEvent(int id, TaskType taskType, Event event) {
        this.id = id;
        this.taskType = taskType;
        this.event = event;
    }
}
