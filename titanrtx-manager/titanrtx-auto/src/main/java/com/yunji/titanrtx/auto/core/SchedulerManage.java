package com.yunji.titanrtx.auto.core;

import com.yunji.titanrtx.auto.config.AutoMsgSender;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.auto.AbstractDeploy;
import com.yunji.titanrtx.common.u.DateU;
import com.yunji.titanrtx.manager.service.BatchCenterService;
import com.yunji.titanrtx.manager.service.SceneOperatingCenterService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 7/4/2020 2:17 下午
 * @Version 1.0
 */
@Slf4j
@Component
public class SchedulerManage implements TaskNotify {
    @Resource
    private SceneOperatingCenterService sceneOperatingCenterService;
    @Resource
    private BatchCenterService batchCenterService;

    @Resource
    private AutoMsgSender autoMsgSender;

    private SchedulerFactory schedulerFactory;
    private Scheduler scheduler;
    private List<JobKey> listJobKey;

    @PostConstruct
    public void init() throws SchedulerException {
        schedulerFactory = new StdSchedulerFactory();
        scheduler = schedulerFactory.getScheduler();
        listJobKey = new ArrayList<>();
    }

    @Override
    public void clear() throws SchedulerException {
        scheduler.deleteJobs(listJobKey);
        listJobKey.clear();
    }

    @Override
    public void update(List<AbstractDeploy> deploys) {
        // 停止所有的定时任务
        try {
            if (scheduler.isStarted()) {
                scheduler.deleteJobs(listJobKey);
                listJobKey.clear();
            }
            deploys.stream().forEach(deploy -> {
                JobBuilder builder = JobBuilder.newJob(TestJob.class);
                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put(GlobalConstants.JOB_DATA_DEFAULT_KEY, deploy);
                jobDataMap.put(GlobalConstants.JOB_DATA_SCENE_OPERATING_CENTER, sceneOperatingCenterService);
                jobDataMap.put(GlobalConstants.JOB_DATA_BATCH_CENTER, batchCenterService);
                jobDataMap.put(GlobalConstants.JOB_DATA_NOTIFY_CONFIG, autoMsgSender);
                JobKey jobKey = new JobKey(Integer.toString(deploy.getId()));
                listJobKey.add(jobKey);
                JobDetail jobDetail = builder.withIdentity(jobKey).setJobData(jobDataMap).build();

                String cron = getCron(deploy);

                log.info("定时压测任务: [ {} ]，cron 语句: [ {} ]", deploy.getId(), cron);
                CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(Integer.toString(deploy.getId()))
                        .withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();

                try {
                    scheduler.scheduleJob(jobDetail, trigger);
                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
            });
            scheduler.start();
        } catch (Exception e) {
            log.error("Update 定时压测任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取cron表达式
     */
    private String getCron(AbstractDeploy deploy) {
        StringBuilder temp = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        switch (deploy.getGrading()) {
            case PRECISE: {
                calendar.setTime(DateU.parseDate(deploy.getTime(), DateU.LONG_PATTERN));
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DATE);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                temp.append(second)
                        .append(" ")
                        .append(minute)
                        .append(" ")
                        .append(hour)
                        .append(" ")
                        .append(day)
                        .append(" ")
                        .append(month)
                        .append(" ")
                        .append("?")
                        .append(" ")
                        .append(year);
            }
            break;
            case EVERYDAY: {
                Date date = DateU.parseDate(deploy.getTime(), "HH点mm分");
                calendar.setTime(date);
                int minute = calendar.get(Calendar.MINUTE);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                temp.append("0")
                        .append(" ")
                        .append(minute)
                        .append(" ")
                        .append(hour)
                        .append(" ")
                        .append("*")
                        .append(" ")
                        .append("*")
                        .append(" ")
                        .append("?")
                        .append(" ")
                        .append("*");
            }
            break;
            case EVERYHOUR: {
                Date date = DateU.parseDate(deploy.getTime(), "mm分");
                calendar.setTime(date);
                int minute = calendar.get(Calendar.MINUTE);
                temp.append("0")
                        .append(" ")
                        .append(minute)
                        .append(" ")
                        .append("*")
                        .append(" ")
                        .append("*")
                        .append(" ")
                        .append("*")
                        .append(" ")
                        .append("?")
                        .append(" ")
                        .append("*");
            }
            break;
            case EVERYMINUTE:
                temp.append("0")
                        .append(" ")
                        .append("*/1")
                        .append(" ")
                        .append("*")
                        .append(" ")
                        .append("*")
                        .append(" ")
                        .append("*")
                        .append(" ")
                        .append("?")
                        .append(" ")
                        .append("*");
                break;
        }
        return temp.toString();
    }
}
