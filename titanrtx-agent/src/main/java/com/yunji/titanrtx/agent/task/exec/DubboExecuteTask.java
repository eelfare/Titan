package com.yunji.titanrtx.agent.task.exec;

import com.yunji.titanrtx.agent.collect.CollectScheduler;
import com.yunji.titanrtx.agent.future.core.DubboFutureCallBack;
import com.yunji.titanrtx.agent.future.fatory.DubboFutureObjectFactory;
import com.yunji.titanrtx.agent.service.LiaisonService;
import com.yunji.titanrtx.agent.service.ParamsService;
import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.DubboService;
import com.yunji.titanrtx.common.domain.task.Task;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import com.yunji.titanrtx.plugin.dubbo.support.DubboAsyncClient;
import com.yunji.titanrtx.plugin.dubbo.support.GenericU;
import com.yunji.titanrtx.plugin.dubbo.support.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.List;

@Slf4j
public class DubboExecuteTask extends AbstractExecute<DubboFutureCallBack> {

    private DubboAsyncClient asyncClient;

    public DubboExecuteTask(TaskDispatcher taskDispatcher,
                            LiaisonService liaisonService,
                            Task task,
                            CollectScheduler collectScheduler, ParamsService paramsService) throws InterruptedException {
        super(taskDispatcher, liaisonService, task, collectScheduler, paramsService);
        init();
    }

    @Override
    protected PooledObjectFactory<DubboFutureCallBack> objectFactory() {
        return new DubboFutureObjectFactory();
    }


    @Override
    public void init() throws InterruptedException {
        initTask();
    }

    protected void detailTaskInit() {
        for (Bullet bullet : task.getBullets()) {
            bulletIdMap.put(bullet.getId(), bullet);
        }
    }

    @Override
    public void doInvoke(Bullet bullet) {
        DubboService dubboService = (DubboService) bullet;
        Integer id = dubboService.getId();
        String paramToUse = getParam(dubboService);
        try {
            RpcRequest request = GenericU.newBuild(
                    dubboService.getServiceName(),
                    dubboService.getMethodName(),
                    dubboService.getParamsType(),
                    paramToUse,
                    dubboService.getAddress(),
                    dubboService.getRpcContent(),
                    dubboService.getClusterAddress());

            DubboFutureCallBack future = objectPool.borrowObject();
            future.init(countDownLatch, collectMap.get(id), objectPool, dubboService.getServiceName() + ":" + dubboService.getMethodName());
            asyncClient.execute(request, future);
        } catch (Exception e) {
            log.error("构建请求对象失败...........................:{}", dubboService);
            e.printStackTrace();
        }
    }

    @Override
    public TaskType getType() {
        return TaskType.DUBBO;
    }


    @Override
    protected void doClose() {
        try {
            asyncClient.close();
        } catch (Exception e) {
            log.error("关闭dubboAsyncClient异常:{}............", e.getMessage());
        }
    }

    @Override
    protected void doPrepareExecute() {
        asyncClient = DubboAsyncClient.createAsyncClient();
        for (Bullet bullet : task.getBullets()) {
            DubboService dubboService = (DubboService) bullet;
            //多个链路的支持
            String clusterAddress = dubboService.getClusterAddress();
            String address;
            if (StringUtils.isNotEmpty(clusterAddress)) {
                address = clusterAddress;
            } else {
                address = dubboService.getAddress();
            }
            log.info("DubboExecute 链路ID:{}, repaired address: {}", dubboService.getId(), address);
            hostMap.putIfAbsent(dubboService.getId(), address);
        }
    }

    @Override
    protected TaskType getTaskType() {
        return TaskType.DUBBO;
    }

    private String getParam(Bullet bullet) {
        if (bullet.getParams() == null) {
            List<String> params = paramsService.selectParamsBatch(bullet.getParamIds());
            log.info("查询Dubbo链路参数size:{}", params.size());
            bullet.setParams(params);
        }
        return randomParam(bullet.getParams());
    }

    @Override
    protected void configObjectPool() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal((int) task.getConcurrent() * 10);
        poolConfig.setMinIdle((int) task.getConcurrent());
        objectPool = new GenericObjectPool<>(objectFactory(), poolConfig);
    }
}
