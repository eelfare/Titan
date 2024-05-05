package com.yunji.titanrtx.auto.boot;

import com.yunji.titanrtx.auto.listener.AutoTestDeployListener;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.register.Register;
import com.yunji.titanrtx.common.zookeeper.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 7/4/2020 10:57 上午
 * @Version 1.0
 */
@Slf4j
@Component
public class AutoTestRegister implements Register {

    @Resource
    private ZookeeperService zookeeperService;

    @Resource
    private AutoTestDeployListener autoTestDeployListener;

    @PostConstruct
    @Override
    public void register() {
        zookeeperService.nodeCacheListener(autoTestDeployListener, GlobalConstants.AUTO_DEPLOYS_PATH);
        //zookeeperService.pathChildrenCacheLister(autoTestDeployListener, GlobalConstants.AUTO_PATH);
    }
}
