package com.yunji.titanrtx.common.alarm;

import com.google.gson.Gson;
import com.yunji.titanrtx.common.message.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.yunji.titanrtx.common.alarm.MessageSender.BEACON_FEISHU_URL;

/**
 * AlarmProperties
 *
 * @author leihz
 * @since 2020-08-12 10:58 上午
 */
@Component
public class AlarmService {

    private static final String AUTO_STRESS_ALERT_ID = "titan_auto_alarm";
    private static final String ALERT_ID_FOR_DEV = "titan_alarm_for_develop";
    //流量构造异常.
    private static final String FLOW_CREATOR_ID = "titan_data_factory";

    /**
     * MAINTAINER 企业微信 => maintainer 告警群
     */
    @Value("${qiwei.maintainer.url:https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=bb89002e-677d-48a7-82bc-9c4fd3eaa293}")
    public String qiwei_maintainer_hook;
    /**
     * AUTO_ALARM 企业微信 => 自动化压测群 告警机器人
     */
    @Value(("${qiwei.auto.group.url:https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=edeea829-224c-47f1-986c-94aa18e605f8}"))
    public String qiwei_auto_group_hook;
    /**
     * PERF_BASELINE 企业微信 => 压测性能基线异常告警
     */
    @Value(("${qiwei.perf.baseline.url:https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=9508da4d-6ce1-4328-aea7-ccaab92deb86}"))
    public String qiwei_perf_baseline_hook;


    //压测性能基线异常告警
    public void send(MessageSender.Type type, String msg) {
        if (StringUtils.isNotEmpty(msg)) {
            //String fsMsg = MessageSender.buildFsMessage(msg, type);

            switch (type) {
                case MAINTAINER:
                    MessageSender.doSend(qiwei_maintainer_hook, msg, null);
                    MessageSender.doSend(BEACON_FEISHU_URL, msg, ALERT_ID_FOR_DEV);
                    break;
                case AUTO_ALARM:
                    MessageSender.doSend(qiwei_auto_group_hook, msg, null);
                    MessageSender.doSend(BEACON_FEISHU_URL, msg, AUTO_STRESS_ALERT_ID);
                    break;
                case PERF_BASELINE:
                    MessageSender.doSend(qiwei_perf_baseline_hook, msg, null);
                    MessageSender.doSend(BEACON_FEISHU_URL, msg, AUTO_STRESS_ALERT_ID);
                    break;

                case FLOW_CREATOR:
                    MessageSender.doSend(BEACON_FEISHU_URL, msg, FLOW_CREATOR_ID);
                    break;
                case CUSTOMER:
                    break;
                default:
                    break;
            }
        }
    }

}
