package com.yunji.titanrtx.auto.usual;

import com.yunji.titanrtx.auto.config.AutoMsgSender;
import com.yunji.titanrtx.common.service.CommanderService;
import com.yunji.titanrtx.common.u.CollectionU;
import com.yunji.titanrtx.common.u.DateU;
import com.yunji.titanrtx.manager.dao.bos.top.TopLinkAsFilterBo;
import com.yunji.titanrtx.manager.service.SceneOperatingCenterService;
import com.yunji.titanrtx.manager.service.report.support.ReportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自动定时压测任务
 *
 * @Author: 景风（彭秋雁）
 * @Date: 27/2/2020 6:04 下午
 * @Version 1.0
 */
@Slf4j
@Component
public class AutoSchedulingTest {
    public AutoSchedulingTest() {
        this(1);
    }

    public AutoSchedulingTest(int type) {
        System.out.println(type);
    }

    @Resource
    SceneOperatingCenterService sceneOperatingCenterService;

    @Resource
    AutoMsgSender notifyConfig;

    @Resource
    CommanderService commanderService;


    // 每周二、四15点抓取top300,并创建场景
    @Scheduled(cron = "${auto.get_top.cron:0 0 15 ? * TUE,THU}")
    public void autoGetTop300() {
        if (!commanderService.getTop300StressSwitch()) {
            log.info("未开启top300自动压测");
            return;
        }
        log.info("正在进行top300数据获取");
        // 删除上一次产生的top300 压测数据
        sceneOperatingCenterService.deleteTips();
        sceneOperatingCenterService.deleteAutoTestSceneData();

        Date now = new Date();
        Date sDate;
        Date eDate;
        if (!ReportUtils.isIDC()) {
            log.info("获取非线上数据");
            // 模拟数据
            sDate = DateU.parseDate("2019-11-13 15:20:58", DateU.LONG_PATTERN);
            eDate = DateU.parseDate("2019-11-13 15:21:00", DateU.LONG_PATTERN);
        } else {
            log.info("获取线上数据");
            sDate = new Date(now.getTime() - 24 * 60 * 60 * 1000);
            eDate = new Date(sDate.getTime() + 2 * 60 * 60 * 1000);
        }

        // 获取top500接口
        List<TopLinkAsFilterBo> topLink = sceneOperatingCenterService.getTopLink(null, null, sDate, eDate, 500, null);
        // 需要提示的接口
        List<TopLinkAsFilterBo> needHintLink = new ArrayList<>();
        AtomicInteger totalNum = new AtomicInteger(0);
        topLink.stream().forEach(link -> {
            if (totalNum.get() < 300 && !link.isBlnBlack() && !link.isBlnWhite()) {
                needHintLink.add(link);
            }
            if (!link.isBlnBlack()) {
                totalNum.getAndIncrement();
            }
        });
        // 如果有需要提示的接口，人工处理的话，则发送机器人消息
        if (!needHintLink.isEmpty()) {
            log.info("获取到的api有需要人工处理");
            // 将数据保存在zk中
            sceneOperatingCenterService.addTipsTopLinks(topLink);
            notifyConfig.alarm(notifyConfig.getDealWithLinkMsgContent(needHintLink));
        } else if (topLink.isEmpty()) { // 本身获取的到链路就为空
            log.warn("没有获取到top300数据");
            notifyConfig.alarm(notifyConfig.getDefaultMsgContent("获取top300失败", notifyConfig.getDeveloperPhone()));
        } else { // 创建top300压测场景
            log.info("正在创建top300创建.....");
            List<TopLinkAsFilterBo> enableLinkList = new ArrayList<>();
            totalNum.getAndSet(0);
            topLink.stream().forEach(link -> {
                if (totalNum.getAndIncrement() <= 300 && !link.isBlnBlack()) {
                    enableLinkList.add(link);
                }
            });
            sceneOperatingCenterService.topLinkToAutoScene(enableLinkList);
        }
    }

    // 每周二、四16-18点来验证是否已经创建好了TOP300场景
    @Scheduled(cron = "0 0 16-23 ? * TUE,THU")
    public void autoVerifyScene() {
        if (!commanderService.getTop300StressSwitch()) {
            log.info("未开启top300自动压测");
            return;
        }
        log.info("正在执行验证[TOP300]场景");
        // 从zk中获取数据
        String sceneData = sceneOperatingCenterService.queryAutoTestSceneData();
        List<TopLinkAsFilterBo> topLinkAsFilterBos = sceneOperatingCenterService.queryTipsTopLinks();
        String msg = notifyConfig.getDefaultMsgContent("自动化创建top 300出现未知错误", notifyConfig.getDeveloperPhone());
        if (StringUtils.isEmpty(sceneData)
                && CollectionU.isNotEmpty(topLinkAsFilterBos)) { // 未能创建成功场景
            msg = notifyConfig.getDealWithLinkMsgContent(topLinkAsFilterBos);
        } else if (StringUtils.isNotEmpty(sceneData) && CollectionU.isEmpty(topLinkAsFilterBos)) {
            try {
                validAndGetSceneId(sceneData);
                // 验证机器是否足够
                if (!sceneOperatingCenterService.checkMachine()) {
                    return;
                }
                msg = notifyConfig.getDefaultMsgContent("当前压测机器不足，请运维同学扩容到40台agent压测机器（最高 80W QPS）", notifyConfig.getMaintainerPhone());
            } catch (Exception e) {
                log.info(e.getMessage());
                msg = notifyConfig.getDefaultMsgContent("自动创建的top 300场景有误", notifyConfig.getDeveloperPhone());
            }
        }
        notifyConfig.alarm(msg);
    }

    // 校验scene
    private Integer validAndGetSceneId(String sceneData) throws Exception {
        String[] split = sceneData.split(",");
        Integer sceneId = Integer.valueOf(split[0]);
        Long createTime = Long.valueOf(split[1]);
        Date date = new Date(createTime);
        Calendar now = Calendar.getInstance();
        Calendar create = Calendar.getInstance();
        now.setTime(new Date());
        create.setTime(date);

        // 判断scene是否有效
        if (!sceneOperatingCenterService.existScene(sceneId)) {
            throw new Exception("场景被删除了");
        }
        return sceneId;
    }
}
