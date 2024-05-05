package com.yunji.titanrtx.manager.boot;

import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.register.Register;
import com.yunji.titanrtx.common.zookeeper.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Component
public class ManagerRegister implements Register {

    @Resource
    private ZookeeperService zookeeperService;

    @Override
    @PostConstruct
    public void register() {
        zookeeperService.createPersistentNode(GlobalConstants.MANAGER_SCENE_LINK_PATH);
        zookeeperService.createPersistentNode(GlobalConstants.MANAGER_AUTO_TEST_SCENE);
        zookeeperService.createPersistentNode(GlobalConstants.AUTO_DEPLOYS_PATH);
    }
}
