package com.yunji.titanrtx.agent.boot;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.meta.AgentMeta;
import com.yunji.titanrtx.common.enums.AgentStatus;
import com.yunji.titanrtx.common.enums.Platform;
import com.yunji.titanrtx.common.register.Register;
import com.yunji.titanrtx.common.zookeeper.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Component
public class AgentRegister implements Register {

    public static AgentMeta AGENT_META;

    public static String AGENT_ADDRESS;

    public static Platform CLOUD_PLATFORM;

    @Resource
    private ZookeeperService zookeeperService;

    @Value("${cloudPlatform}")
    private String cloudPlatform;


    @Override
    @PostConstruct
    public void register() {
        if (StringUtils.equalsIgnoreCase(cloudPlatform ,Platform.TENCENT.toString())){
            CLOUD_PLATFORM = Platform.TENCENT;
        }else {
            CLOUD_PLATFORM = Platform.ALI;
        }
        AGENT_META = new AgentMeta().build(CLOUD_PLATFORM);
        AGENT_ADDRESS = zookeeperService.register(GlobalConstants.AGENT_PATH + "/", JSON.toJSONString(AGENT_META));
        log.info("注册zk成功AgentMeta:{},..............",AGENT_META);
    }


    public void update(AgentStatus status){
        AGENT_META.setAgentStatus(status);
        zookeeperService.update(AGENT_ADDRESS,JSON.toJSONString(AGENT_META));
    }

    public AgentStatus getAgentStatus(){
        return AGENT_META.getAgentStatus();
    }


}
