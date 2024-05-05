package com.yunji.titanrtx.commander.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yunji.titanrtx.commander.service.LiaisonService;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.meta.AgentMeta;
import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.zookeeper.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LiaisonServiceImpl implements LiaisonService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ZookeeperService zookeeperService;


    private static Map<String, DistributedBarrier> barrierLister = new ConcurrentHashMap<>();

    @Override
    public List<AgentMeta> agentMetas() {
        List<String> childNodes = zookeeperService.childNodes(GlobalConstants.AGENT_PATH);
        List<AgentMeta> metas = new ArrayList<>(childNodes.size());
        for (String agentNode : childNodes) {
            AgentMeta meta = JSON.parseObject(zookeeperService.getData(GlobalConstants.AGENT_PATH + "/" + agentNode), AgentMeta.class);
            metas.add(meta);
        }
        return metas;
    }


    public List<String> getAgentAddress(List<AgentMeta> agentMetas) {
        List<String> agentAddress = new ArrayList<>(agentMetas.size());
        for (AgentMeta meta : agentMetas) {
            agentAddress.add(meta.getAddress());
        }
        return agentAddress;
    }


    @Override
    public void hookTaskReportLock(String taskNo) {
        zookeeperService.createPersistentNode(GlobalConstants.LOCK_TASK_REPORT_PATH + "/" + taskNo);
    }


    @Override
    public InterProcessMutex acquireTaskStartLock() throws Exception {
        return zookeeperService.acquireLock(GlobalConstants.LOCK_TASK_DOWN_PATH);
    }

    @Override
    public InterProcessMutex acquireTaskStartLock(long time, TimeUnit unit) throws Exception {
        return zookeeperService.acquireLock(GlobalConstants.LOCK_TASK_DOWN_PATH, time, unit);
    }

    @Override
    public InterProcessMutex acquireTaskReportLock(String taskNo) throws Exception {
        return zookeeperService.acquireLock(GlobalConstants.LOCK_TASK_REPORT_PATH + "/" + taskNo);
    }

    @Override
    public void setBarrier(String taskNo) {
        DistributedBarrier barrier = zookeeperService.setBarrier(GlobalConstants.BARRIER_PATH + "/" + taskNo);
        barrierLister.put(taskNo, barrier);
    }

    @Override
    public void openBarrier(String taskNo) {
        DistributedBarrier barrier = barrierLister.get(taskNo);
        if (barrier != null) {
            try {
                barrier.removeBarrier();
                log.info("移除栅栏成功................taskNo:{}", taskNo);
            } catch (Exception e) {
                log.error("移除栅栏异常................taskNo:{},e:{}", taskNo, e.getMessage());
            }
            barrierLister.remove(taskNo);
        }
    }

    @Override
    public List<String> queryPressureAgent() {
        String id = RpcContext.getContext().getAttachment(GlobalConstants.TASK_ID);
        String type = RpcContext.getContext().getAttachment(GlobalConstants.TASK_TYPE);
        if (StringUtils.isAllBlank(id, type)) return new ArrayList<>();
        return queryPressureAgent(TaskType.valueOf(type), Integer.valueOf(id));
    }

    public List<String> queryPressureAgent(TaskType taskType, Integer id) {
        String jsonAgents = (String) stringRedisTemplate.opsForHash().get(GlobalConstants.TITAN_RTX_PRESSURE_AGENT, taskType.toString() + ":" + id);
        List<String> agentAddress = JSON.parseArray(jsonAgents, String.class);
        return agentAddress == null ? new ArrayList<>() : agentAddress;
    }

    @Override
    public void savePressureAgent(TaskType taskType, Integer id, List<String> agents) {
        stringRedisTemplate.opsForHash().put(GlobalConstants.TITAN_RTX_PRESSURE_AGENT, taskType.toString() + ":" + id, JSON.toJSONString(agents));
    }

    @Override
    public void deletePressureAgent(TaskType taskType, Integer id) {
        stringRedisTemplate.opsForHash().delete(GlobalConstants.TITAN_RTX_PRESSURE_AGENT, taskType.toString() + ":" + id);
    }

    public Statistics queryPressureStatistics(String taskNo) {
        String jsonObject = (String) stringRedisTemplate.opsForHash().get(GlobalConstants.TITAN_RTX_PRESSURE_STATISTICS, taskNo);
        return JSON.parseObject(jsonObject, Statistics.class);
    }

    @Override
    public void savePressureStatistics(Statistics statistics) {
        SerializerFeature[] features = new SerializerFeature[]{
                SerializerFeature.WriteClassName,
        };
        stringRedisTemplate.opsForHash().put(GlobalConstants.TITAN_RTX_PRESSURE_STATISTICS, statistics.getTaskNo(), JSON.toJSONString(statistics, features));
    }

    @Override
    public void deletedPressureStatistics(String taskNo) {
        stringRedisTemplate.opsForHash().delete(GlobalConstants.TITAN_RTX_PRESSURE_STATISTICS, taskNo);
    }

    @Override
    public void cancelTaskReportLock(String taskNo) {
        zookeeperService.deleteNodeIfExist(GlobalConstants.LOCK_TASK_REPORT_PATH + "/" + taskNo);
    }

    @Override
    public void ensureRemoteDomain(String address) {
        RpcContext.getContext().setAttachment(Constants.REMOTE_DOMAIN, address);
    }

    @Override
    public String getData(String zkPath) {
        return zookeeperService.getData(zkPath);
    }

    @Override
    public void updateData(String zkPath, String data) {
        zookeeperService.update(zkPath, data);
    }

    @Override
    public Boolean top300StressSwitch(Boolean topSwitch) {
        if (topSwitch == null) {
            return false;
        }
        stringRedisTemplate.opsForValue().set(GlobalConstants.AUTO_TOP_SWITCH_KEY, String.valueOf(topSwitch));
        return true;
    }

    @Override
    public Boolean getTop300StressSwitch() {
        if (!stringRedisTemplate.hasKey(GlobalConstants.AUTO_TOP_SWITCH_KEY)) {
            return false;
        }
        String result = stringRedisTemplate.opsForValue().get(GlobalConstants.AUTO_TOP_SWITCH_KEY);
        if ("true".equals(result)) {
            return true;
        }
        return false;
    }
}
