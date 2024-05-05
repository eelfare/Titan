package com.yunji.titanrtx.manager.boot;

import com.yunji.titanrtx.common.annotation.TaskAnnotation;
import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.task.Report;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReportHandler implements ApplicationContextAware, Report {

    private static final Map<TaskType, Report> handlerMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(TaskAnnotation.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            TaskType type = entry.getValue().getClass().getAnnotation(TaskAnnotation.class).type();
            handlerMap.put(type, (Report) entry.getValue());
        }
    }

    @Override
    public void report(Statistics statistics) {
        if (statistics.getTaskType() == TaskType.HTTP) {
            handlerMap.get(TaskType.OUTSIDE).report(statistics);
        }
        handlerMap.get(statistics.getTaskType()).report(statistics);
    }
}
