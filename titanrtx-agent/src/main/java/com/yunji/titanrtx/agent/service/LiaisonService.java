package com.yunji.titanrtx.agent.service;

import com.yunji.titanrtx.common.enums.AgentStatus;

import java.util.concurrent.TimeUnit;

public interface LiaisonService {

    void waitOnBarrier(String path, long maxWait, TimeUnit unit);

    void updateAgentStatus(AgentStatus agentStatus);

    AgentStatus agentStatus();

    void downLine();

}
