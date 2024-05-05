package com.yunji.titanrtx.agent.service.impl;

import com.yunji.titanrtx.agent.boot.AgentRegister;
import com.yunji.titanrtx.agent.service.LiaisonService;
import com.yunji.titanrtx.common.enums.AgentStatus;
import com.yunji.titanrtx.common.zookeeper.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LiaisonServiceImpl implements LiaisonService {


    @Resource
    private ZookeeperService zookeeperService;

    @Resource
    private AgentRegister agentRegister;


    @Override
    public void waitOnBarrier(String path, long maxWait, TimeUnit unit) {
        zookeeperService.waitOnBarrier(path, maxWait, unit);
    }

    @Override
    public void updateAgentStatus(AgentStatus agentStatus) {
        log.info("........ Agent status update to [{}] ........", agentStatus.getMemo());
        agentRegister.update(agentStatus);
    }

    @Override
    public AgentStatus agentStatus() {
        return agentRegister.getAgentStatus();
    }

    @Override
    public void downLine() {
        zookeeperService.deleteNodeIfExist(AgentRegister.AGENT_ADDRESS);
    }
}
