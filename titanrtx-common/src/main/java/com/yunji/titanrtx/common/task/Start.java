package com.yunji.titanrtx.common.task;

import com.yunji.titanrtx.common.domain.task.Task;
import com.yunji.titanrtx.common.message.RespMsg;

public interface Start {

    RespMsg start(Task task) throws InterruptedException;

}
