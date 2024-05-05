package com.yunji.titanrtx.common.alarm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FilterAlarmService
 *
 * @author leihz
 * @since 2020-09-23 09:20 上午
 */
@Component
@Slf4j
public class FilterAlarmService implements InitializingBean {

    private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

    private Map<String, Long> alarmRecordMap = new ConcurrentHashMap<>();

    @Autowired
    private AlarmService alarmService;

    /**
     * @param msgId    消息id,区分一类消息
     * @param duration 间隔发送时间
     * @param msg      消息内容
     */
    public void filterAlarm(String msgId, long duration, MessageSender.Type type, String msg) {
        alarmRecordMap.compute(msgId, (k, v) -> {
            long now = System.currentTimeMillis();
            if (v == null) {
                doSendMsg(type, msg);
            } else {
                if (now - v > duration) {
                    doSendMsg(type, msg);
                } else {
                    log.info("MsgId:{},ts:{},没有超过 duration:{},不告警.", msgId, v, duration);
                }
            }
            v = System.currentTimeMillis();
            return v;
        });
    }

    @Override
    public void afterPropertiesSet() {
        try {
            ses.scheduleWithFixedDelay(new Scanner(), 0L, 60L, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("启动[ alarmRecord scanner ]定时任务成功.");
    }

    private void doSendMsg(MessageSender.Type type, String message) {
        log.info("FilterAlarm 准备告警: {}", type);
        alarmService.send(type, message);
    }

    private class Scanner implements Runnable {
        @Override
        public void run() {
            alarmRecordMap.entrySet().removeIf(entry -> entry.getValue() != null && System.currentTimeMillis() - entry.getValue() > 24 * 3600_000);
        }
    }

}
