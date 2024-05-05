package com.yunji.titanrtx.auto.core;

import com.yunji.titanrtx.common.domain.auto.AbstractDeploy;
import org.quartz.SchedulerException;

import java.util.List;

/**
 * 任务通知
 * @Author: 景风（彭秋雁）
 * @Date: 7/4/2020 11:26 上午
 * @Version 1.0
 */
public interface TaskNotify {
    void clear() throws SchedulerException; // 清空
    void update(List<AbstractDeploy> deploy); // 更新定时任务
}
