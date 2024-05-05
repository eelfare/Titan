package com.yunji.titanrtx.auto.listener;

import com.yunji.titanrtx.auto.core.TaskExecute;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.auto.AbstractDeploy;
import com.yunji.titanrtx.common.service.CommanderService;
import com.yunji.titanrtx.common.zookeeper.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 7/4/2020 11:01 上午
 * @Version 1.0
 */
@Slf4j
@Component
public class AutoTestDeployListener implements NodeCacheListener {

    @Resource
    private TaskExecute execute;

    @Resource
    private CommanderService commanderService;

    @Resource
    private ZookeeperService zookeeperService;

    @Override
    public void nodeChanged() throws Exception {
        String path = GlobalConstants.AUTO_DEPLOYS_PATH;
        String data = zookeeperService.getData(path);

        log.info("自动化压测调度节点:[ {} ] 内容发生变化, 数据：[ {} ].", GlobalConstants.AUTO_DEPLOYS_PATH, data);

        List<AbstractDeploy> listAutoDeploy = commanderService.listAutoDeploy();

        execute.notifyTask(listAutoDeploy);
    }
}
