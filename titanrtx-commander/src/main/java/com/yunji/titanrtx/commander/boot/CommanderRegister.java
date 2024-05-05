package com.yunji.titanrtx.commander.boot;

import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.register.Register;
import com.yunji.titanrtx.common.zookeeper.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Component
public class CommanderRegister implements Register {

    @Resource
    private ZookeeperService zookeeperService;

    @Override
    @PostConstruct
    public void register() {
        zookeeperService.createPersistentNode(GlobalConstants.AGENT_PATH);
        zookeeperService.createPersistentNode(GlobalConstants.BARRIER_PATH);
        zookeeperService.createPersistentNode(GlobalConstants.LOCK_TASK_DOWN_PATH);
        zookeeperService.deleteNodeIfExist(GlobalConstants.LOCK_TASK_REPORT_PATH);
        zookeeperService.createPersistentNode(GlobalConstants.LOCK_TASK_REPORT_PATH);

        zookeeperService.createPersistentNode(GlobalConstants.CIA_RULES_PATH);
    }
}
