package com.yunji.titanrtx.auto.core;

import com.yunji.titanrtx.auto.config.AutoMsgSender;
import com.yunji.titanrtx.auto.support.AutoStressUtils;
import com.yunji.titanrtx.auto.support.ExecutorUtils;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.auto.AbstractDeploy;
import com.yunji.titanrtx.common.domain.auto.BatchDeploy;
import com.yunji.titanrtx.common.domain.auto.CommonStressDeploy;
import com.yunji.titanrtx.common.domain.auto.TopStressDeploy;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.common.u.LocalDateU;
import com.yunji.titanrtx.manager.service.BatchCenterService;
import com.yunji.titanrtx.manager.service.SceneOperatingCenterService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.concurrent.TimeUnit;

/**
 * 压测任务
 *
 * @Author: 景风（彭秋雁）
 * @Date: 7/4/2020 2:54 下午
 * @Version 1.0
 */
@Slf4j
@Data
public class TestJob implements Job {
    private SceneOperatingCenterService sceneOperatingCenterService;

    private AutoMsgSender autoMsgSender;

    private BatchCenterService batchCenterService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        AbstractDeploy deploy = (AbstractDeploy) jobDataMap.get(GlobalConstants.JOB_DATA_DEFAULT_KEY);
        sceneOperatingCenterService = (SceneOperatingCenterService) jobDataMap.get(GlobalConstants.JOB_DATA_SCENE_OPERATING_CENTER);
        batchCenterService = (BatchCenterService) jobDataMap.get(GlobalConstants.JOB_DATA_BATCH_CENTER);
        autoMsgSender = (AutoMsgSender) jobDataMap.get(GlobalConstants.JOB_DATA_NOTIFY_CONFIG);

        //批次任务
        if (deploy instanceof BatchDeploy) {
            String message = "环境 [" + CommonU.getConfigEnv().toUpperCase() + "]: 流量构造场景 [" + deploy.getName() + "] 开始执行 \n\n";

            log.info(message);
            autoMsgSender.alarm(message);

            executeBatch((BatchDeploy) deploy);
        } else {
            //通用压测
            CommonStressDeploy commonStressDeploy = (CommonStressDeploy) deploy;
            // 开始压测
            if (commonStressDeploy.getContinuousTime() > 0) {
                long requestTotal = AutoStressUtils.resetSceneRequestTotal((CommonStressDeploy) deploy, sceneOperatingCenterService);
                String message = "环境 [" + CommonU.getConfigEnv().toUpperCase() + "]: 自动化压测场景 [" + deploy.getName() + "] 开始执行压测\n\n" +
                        " 重设场景请求次数:[" + requestTotal + "]\n" +
                        " 压测持续时间: " + ((CommonStressDeploy) deploy).getContinuousTime() + "min.";

                log.info(message);
                autoMsgSender.alarm(message);
            }
            executeTest(deploy);
        }
    }

    /**
     * 执行流量构造
     *
     * @param deploy
     */
    private void executeBatch(BatchDeploy deploy) {
        String msg = null;
        boolean success = true;
        try {
            // 重置批次
            if (!batchCenterService.reset(deploy.getBatchId())) {
                throw new Exception("批次充值失败");
            }
            // 批次执行
            batchCenterService.start(deploy.getBatchId());
        } catch (Exception ex) {
            success = false;
            msg = autoMsgSender
                    .getDefaultMsgContent("流量构造批次【" + deploy.getName() + "】定时调度执行失败 失败原因： " + ex.getMessage(),
                            autoMsgSender.getDeveloperPhone()
                    );
        }
        if (!success) {
            autoMsgSender.alarm(msg);
        }
    }

    /**
     * 执行压测
     */
    private void executeTest(AbstractDeploy deploy) {
        CommonStressDeploy commonStressDeploy = (CommonStressDeploy) deploy;
        boolean isSuccess = true;
        String msg = null;
        try {
            if (deploy instanceof TopStressDeploy) {
                TopStressDeploy topStressDeploy = (TopStressDeploy) deploy;
                if (topStressDeploy.getTopOrder() != null) {
                    // 重置场景压测目标为第一波压测
                    if (!sceneOperatingCenterService.resetSceneToTarget(topStressDeploy.getSceneId(), topStressDeploy.getTopOrder())) {
                        throw new Exception("场景重置失败");
                    }
                    // 检查top压测是否在规定时间内.
                    // 任何时候自动化top300 必须在规定的时间段才能压
                    if (topStressDeploy.getTopOrder() != TopStressDeploy.TopOrder.DEFAULT && LocalDateU.getCurrentHour() > 6) {
                        throw new Exception("当前时间段不能进行压测");
                    }
                }
            }

            RespMsg respMsg = sceneOperatingCenterService.start(commonStressDeploy.getSceneId());
            if (respMsg.getCode() == 2002) {
                log.warn("当前场景 {} 正在压测中,本次自动化压测忽略.", commonStressDeploy.getSceneId());
                return;
            }

            if (!respMsg.isSuccess()) {
                throw new Exception(respMsg.getMsg());
            }
        } catch (Exception e) {
            isSuccess = false;
            String message = e.getMessage();
            log.error("TestJob 定时压测异常,原因: " + message);
            if (!message.contains("Invoke remote method timeout")) { // 超时的就不发送警告了
                msg = autoMsgSender.getDefaultMsgContent(deploy instanceof TopStressDeploy ?
                                "第[" + ((TopStressDeploy) deploy).getTopOrder().ordinal() + "]波 Top300 定时压测任务执行失败"
                                : "[自动化压测]: 场景[" + deploy.getName() + "] 执行压测失败，原因: " + message,
                        autoMsgSender.getDeveloperPhone());
            }
        }
        //没有成功才发通知.
        if (!isSuccess) {
            autoMsgSender.alarm(msg);
        }
        //构造定时任务停止 job.
        executeStopStress((CommonStressDeploy) deploy);
    }

//    private void scheduleStopTask(AutoTestDeploy deploy) {
//        if (deploy.getContinuousTime() == 0) {
//            deploy.setContinuousTime(1);
//            String msg = "[TestJob] 压测持续时间为0,手动设置为1min.";
//            log.warn(msg);
//            notifyConfig.sendMsgToWeChatBusiness(msg);
//        }
//    }

    /**
     * 执行停止压测
     */
    private void executeStopStress(CommonStressDeploy deploy) {
        if (deploy.getContinuousTime() > 0) {
            ExecutorUtils.schedule(() -> {
                log.info("[{}] 准备停止任务 -> {}", deploy.getClass(), deploy);

                try {
                    RespMsg respMsg = sceneOperatingCenterService.stop(deploy.getSceneId());
                    if (!respMsg.isSuccess()) {
                        throw new Exception(respMsg.getMsg());
                    }
                    log.info("[TestJob] 停止压测任务成功 -> {}", deploy);
                } catch (Exception e) {
                    log.error("TestJob 停止压测任务失败,原因: " + e.getMessage());
                    String stopMsg = autoMsgSender.getDefaultMsgContent(
                            "[自动化压测]: 场景[" + deploy.getName() + "] 停止压测失败，原因: " + e.getMessage(),
                            autoMsgSender.getDeveloperPhone());

                    autoMsgSender.alarm(stopMsg);
                }
            }, deploy.getContinuousTime(), TimeUnit.MINUTES);

            log.info("[TestJob] 构造定时停止压测任务成功, {} min 后停止任务.", deploy.getContinuousTime());
        } else {
            String msg = "[TestJob] 压测持续时间设置为" + deploy.getContinuousTime() + ",请检查,并手工停止压测任务.";
            log.error(msg);
            autoMsgSender.alarm(msg);
        }
    }
}
