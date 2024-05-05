package com.yunji.titanrtx.auto.core;

import com.yunji.titanrtx.common.domain.auto.AbstractDeploy;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 压测任务执行
 *
 * @Author: 景风（彭秋雁）
 * @Date: 7/4/2020 11:17 上午
 * @Version 1.0
 */
@Slf4j
@Component
public class TaskExecute implements TaskNotify {
    @Resource
    SchedulerManage manage;

    public void notifyTask(List<AbstractDeploy> deploys) throws SchedulerException {
        if (deploys == null || deploys.isEmpty()) { // 关闭所有的未执行压测的任务
            clear();
            return;
        }
        // 校验任务
        update(deploys);
    }

    @Override
    public void clear() throws SchedulerException {
        manage.clear();
    }

    @Override
    public void update(List<AbstractDeploy> deploy) {
        manage.update(deploy);
    }
}
