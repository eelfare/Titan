package com.yunji.titanrtx.auto.config;

import com.yunji.titanrtx.common.alarm.AlarmService;
import com.yunji.titanrtx.common.alarm.MessageSender;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.top.TopLinkAsFilterBo;
import com.yunji.titanrtx.manager.service.report.support.ReportUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 23/4/2020 10:49 上午
 * @Version 1.0
 */
@Slf4j
@Component
@Data
public class AutoMsgSender {
    @Value("${deleoper_hone:17688969083}") // 开发人员电话
    private String developerPhone;

    @Value("${maintainer_hone:15708453343}") // 运维人员电话
    private String maintainerPhone;

    @Autowired
    private AlarmService alarmService;

    // 发送消息到企业微信群
    public void alarm(String msg) {
        if (!ReportUtils.isIDC() || StringUtils.isEmpty(msg)) {
            log.warn(msg);
            // 非IDC环境不发消息
            return;
        }
        alarmService.send(MessageSender.Type.AUTO_ALARM, msg);
    }


    // 获取消息内容
    public String getDefaultMsgContent(String content, String phoneStr) {
        return content;
    }

    // 获取人工处理top link的消息体
    public String getDealWithLinkMsgContent(List<TopLinkAsFilterBo> needHintLink) {
        StringBuilder tips = new StringBuilder();

        tips
                .append("环境[").append(CommonU.getConfigEnv().toUpperCase()).append("]: ")
                .append("日常TOP300自动化压测")
                .append("\n")
                .append("出现部分警告接口：")
                .append("\n");
        AtomicInteger total = new AtomicInteger(0);
        for (TopLinkAsFilterBo link : needHintLink) {
            tips.append(total.incrementAndGet()).append(". ").append(link.getDomain()).append(link.getPath()).append("\n");
            if (total.get() == 3) {
                tips.append(total.incrementAndGet()).append(". ").append("...").append("\n");
                break;
            }
        }
        tips.append("\n---").append("\n").append("请相关人员及时处理");

        return tips.toString();
    }
}
