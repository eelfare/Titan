package com.yunji.titanrtx.agent.task;

import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.task.Collect;
import com.yunji.titanrtx.common.task.Progress;
import com.yunji.titanrtx.common.task.Report;

public interface Execute extends Progress, Runnable, Report, Collect {

    void init() throws InterruptedException;

    void doInvoke(Bullet bullet);

    RespMsg doStop();

    TaskType getType();

}
