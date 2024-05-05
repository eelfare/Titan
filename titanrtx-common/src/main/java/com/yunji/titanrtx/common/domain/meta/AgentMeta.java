package com.yunji.titanrtx.common.domain.meta;

import com.yunji.titanrtx.common.enums.AgentStatus;
import com.yunji.titanrtx.common.enums.Platform;
import com.yunji.titanrtx.common.u.NetU;
import lombok.Data;

import java.io.Serializable;

@Data
public class AgentMeta implements Serializable {

    private String id;

    private String address;

    private AgentStatus agentStatus;

    private long upTime;

    public AgentMeta build(Platform platform) {
        address = NetU.getLocalHost();
        agentStatus = AgentStatus.IDLE;
        upTime = System.currentTimeMillis();
        id = platform.getInstanceId();
        return this;
    }
}
