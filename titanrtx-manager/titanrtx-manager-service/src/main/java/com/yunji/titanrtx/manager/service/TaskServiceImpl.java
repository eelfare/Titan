package com.yunji.titanrtx.manager.service;

import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.meta.AgentMeta;
import com.yunji.titanrtx.common.domain.progress.Progress;
import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.Pair;
import com.yunji.titanrtx.common.domain.task.Task;
import com.yunji.titanrtx.common.enums.AgentStatus;
import com.yunji.titanrtx.common.enums.Allot;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.CommanderService;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.http.AgentInfoBo;
import com.yunji.titanrtx.manager.dao.bos.http.SceneProgressBo;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.yunji.titanrtx.manager.service.report.support.ReportLog.log;

@Service
public class TaskServiceImpl implements TaskService {

    @Resource
    private CommanderService commanderService;
    @Resource
    private HttpSceneService httpSceneService;


    @Override
    public boolean checkStart(long concurrent, long singleMachineConcurrent) {
        AgentInfoBo agentInfo = agentInfoBo();
        int size = agentInfo.getAvailable().size();
        return (size * singleMachineConcurrent == 0) || size * singleMachineConcurrent < concurrent;
    }


    @Override
    public AgentInfoBo agentInfoBo() {
        List<AgentMeta> metas = commanderService.agentMetas();
        List<String> onlineAgents = new ArrayList<>();
        List<String> runningAgents = new ArrayList<>();
        List<String> availableAgents = new ArrayList<>();
        List<String> disableAgents = new ArrayList<>();
        for (AgentMeta meta : metas) {
            String address = meta.getAddress();
            onlineAgents.add(address);
            if (AgentStatus.IDLE == meta.getAgentStatus()) {
                availableAgents.add(address);
            } else if (AgentStatus.RUNNING == meta.getAgentStatus()) {
                runningAgents.add(address);
            } else {
                disableAgents.add(address);
            }
        }
        return new AgentInfoBo(onlineAgents, runningAgents, availableAgents, disableAgents);
    }

    @Override
    public List<String> instanceIds() {
        List<AgentMeta> metas = commanderService.agentMetas();
        List<String> ins = new ArrayList<>(metas.size());
        for (AgentMeta meta : metas) {
            ins.add(meta.getId());
        }
        return ins;
    }

    @Override
    public RespMsg doStart(BaseEntity sceneEntity, List<Bullet> bulletEntity, int machineMaxConcurrent, TaskType taskType) throws InterruptedException {
        Task task = new Task();
        if (taskType == TaskType.HTTP) {
            // alertInfo begin.
            Pair<String, String> alertInfo = httpSceneService.getAlertInfo(sceneEntity.getId());
            log.info("Titan do start stress get alert info [ {} ] and fill in task.", alertInfo);
            String alertWebHook = alertInfo.getKey();
            if (StringUtils.isNotEmpty(alertWebHook)) {
                task.setAlertHook(alertWebHook);
            }
        }
        // alertInfo end.
        task.setTaskType(taskType);
        task.setSingleMachineConcurrent(machineMaxConcurrent);
        task.setStartTime(new Date());
        BeanUtils.copyProperties(sceneEntity, task);
        task.setBullets(bulletEntity);
        return commanderService.start(task);
    }

    @Override
    public RespMsg doStop(Integer id, TaskType taskType) {
        ensureAttachment(id, taskType);
        return commanderService.stop();
    }

    @Override
    public void restartAll() {
        commanderService.restart();
    }

    @Override
    public void reset() {
        commanderService.reset();
    }

    @SuppressWarnings("unchecked")
    @Override
    public SceneProgressBo progress(Integer id, TaskType type) {
        ensureAttachment(id, type);
        RespMsg respMsg = commanderService.progress();
        if (respMsg.isSuccess()) {
            List<Progress> progressMetas = (List<Progress>) respMsg.getData();
            return fuseAverageProgressMeta(progressMetas);
        }
        return null;
    }


    private void ensureAttachment(Integer id, TaskType taskType) {
        RpcContext.getContext().setAttachment(GlobalConstants.TASK_ID, String.valueOf(id));
        RpcContext.getContext().setAttachment(GlobalConstants.TASK_TYPE, taskType.name());
    }


    @Override
    public List<Bullet> buildBullet(List<? extends BaseEntity> entities, Allot allot, Class<? extends Bullet> clazz) throws Exception {
        List<Bullet> bullets = new ArrayList<>(entities.size());
        if (allot == Allot.QPS) { // 如果是QPS，则需要重新计算权重
            // 获取最大值
            List<LinkEntity> list = (List<LinkEntity>) entities;
            long maxQps = list.stream().mapToLong(LinkEntity::getQps).max().getAsLong();
            for (LinkEntity e : list) {
                Bullet instance = clazz.newInstance();
                BeanUtils.copyProperties(e, instance);
                // 重新设置权重
                long newQps = (long) Math.ceil((100 * e.getQps()) / (maxQps * 1.0));
                instance.setWeight(newQps);
                bullets.add(instance);
            }
        } else {
            for (BaseEntity e : entities) {
                Bullet instance = clazz.newInstance();
                BeanUtils.copyProperties(e, instance);
                bullets.add(instance);
            }
        }

        return bullets;
    }


    private SceneProgressBo fuseAverageProgressMeta(List<Progress> progressMetas) {
        SceneProgressBo bo = new SceneProgressBo();
        long requestTimes = 0, waitResponseTimes = 0, total = 0;

        for (Progress meta : progressMetas) {
            requestTimes += meta.getRequestTimes();
            waitResponseTimes += meta.getWaitResponseTimes();
            total += meta.getTotal();
        }
        bo.setRequestRate(CommonU.divideRateNoSign(requestTimes, total, 2));
        bo.setWaitResponseRate(CommonU.divideRateNoSign(total - waitResponseTimes, total, 2));
        return bo;
    }


}
