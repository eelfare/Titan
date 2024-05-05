package com.yunji.titanrtx.cia.agent.boot;

import com.yunji.titanrtx.cia.agent.log.listener.CiaConfigListener;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.register.Register;
import com.yunji.titanrtx.common.zookeeper.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Component
public class CiaAgentRegister implements Register {


    @Resource
    private ZookeeperService zookeeperService;


    @Resource
    CiaConfigListener ciaConfigListener;


    @Override
    @PostConstruct
    public void register() {
        zookeeperService.pathChildrenCacheLister(ciaConfigListener, GlobalConstants.CIA_PATH);
    }



}
