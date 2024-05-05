package com.yunji.titanrtx.agent.task.exec;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.agent.collect.CollectScheduler;
import com.yunji.titanrtx.agent.collect.CollectorUtils;
import com.yunji.titanrtx.agent.future.core.DubboFutureCallBack;
import com.yunji.titanrtx.agent.future.fatory.DubboFutureObjectFactory;
import com.yunji.titanrtx.agent.service.LiaisonService;
import com.yunji.titanrtx.agent.service.ParamsService;
import com.yunji.titanrtx.agent.task.exec.dubbo.DubboAsyncFutureClient;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.statistics.StatisticsDetail;
import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.DubboService;
import com.yunji.titanrtx.common.domain.task.ParamRange;
import com.yunji.titanrtx.common.domain.task.Task;
import com.yunji.titanrtx.common.enums.ParamMode;
import com.yunji.titanrtx.common.enums.ParamTransmit;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.resp.RespCodeOperator;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import com.yunji.titanrtx.common.u.NamedThreadFactory;
import com.yunji.titanrtx.plugin.dubbo.support.GenericU;
import com.yunji.titanrtx.plugin.dubbo.support.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.dubbo.rpc.RpcException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.yunji.titanrtx.common.GlobalConstants.PARAM_TRANSMIT_BOUNDARY;
import static com.yunji.titanrtx.common.enums.ParamTransmit.*;
import static com.yunji.titanrtx.common.u.LogU.info;

@Slf4j
public class DubboNewExecuteTask extends AbstractExecute<DubboFutureCallBack> {

    private DubboAsyncFutureClient asyncClient;

    public DubboNewExecuteTask(TaskDispatcher taskDispatcher,
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
        log.info("[DUBBO] DubboNewExecuteTask detailTaskInit is initiation.");
        paramSetoutLatch = new CountDownLatch((int) task.getBullets().size());
        this.urlIdMap = new HashMap<>();
        this.paramsBlockingQueueMap = new HashMap<>();
        this.linkLumpSizeMap = new HashMap<>();
        for (Bullet bullet : task.getBullets()) {
            bulletIdMap.put(bullet.getId(), bullet);
            Integer linkId = bullet.getId();
            BlockingQueue<List<String>> queue = new LinkedBlockingQueue<>(ORDER_QUEUE_CAPACITY);
            this.paramsBlockingQueueMap.put(linkId, queue);
            int qps = (int) (bullet.getWeight() * task.getConcurrent() / totalWeight(task.getBullets()));
            log.info("[DUBBO] 链路:{} 的weight={} 所占用的qps为：{}", bullet.getId(), bullet.getWeight(), qps);
            this.linkLumpSizeMap.put(linkId, qps);
        }
    }

    @Override
    protected void configObjectPool() {
        objectPool = null;
    }

    @Override
    public void doInvoke(Bullet bullet) {
        DubboService dubboService = (DubboService) bullet;
        Integer id = dubboService.getId();
        /* String paramToUse = getParam(dubboService);*/
        String paramToUse = getParam0(dubboService);
        try {
            RpcRequest request = GenericU.newBuild(
                    dubboService.getServiceName(),
                    dubboService.getMethodName(),
                    dubboService.getParamsType(),
                    paramToUse,
                    dubboService.getAddress(),
                    dubboService.getRpcContent(),
                    dubboService.getClusterAddress());

            CompletableFuture<Object> resultFuture = asyncClient.execute(request);
            long startTime = System.currentTimeMillis();
            resultFuture.whenComplete((retValue, exception) -> {
                if (exception == null) {
                    doCompleted(retValue, dubboService, startTime);
                } else {
                    doFailed(new RpcException(exception), dubboService);
                }
            });
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
    protected void doPrepareExecute() throws InterruptedException {
        asyncClient = DubboAsyncFutureClient.createAsyncClient();
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

            log.info("DubboExecute 链路ID:{},service:{}, repaired address: {}",
                    dubboService.getId(), dubboService.getServiceName(), address);

            hostMap.putIfAbsent(dubboService.getId(), address);
        }
        //参数获取初始化
        initDubboParamsTask();
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


    private void doCompleted(Object retValue, DubboService dubboService, long startTime) {
        Integer respCode = RespCodeOperator.getRespCode(JSON.toJSONString(retValue));

        CollectorUtils.collectSuccessResponse(openAgentCollectQps, respCode,
                dubboService.getServiceName() + ":" + dubboService.getMethodName());

        StatisticsDetail statisticsDetail = collectMap.get(dubboService.getId());

        statisticsDetail.addStatusCode(GlobalConstants.HTTP_SUCCESS_CODE);
        long duration = System.currentTimeMillis() - startTime;
        long totalDuration = duration < 0 ? 0 : duration;
        statisticsDetail.setDuration(statisticsDetail.getDuration() + totalDuration);
        int logicCode;
        try {
            logicCode = RespCodeOperator.getRespCode(JSON.toJSONString(retValue));
        } catch (Exception e) {
            logicCode = GlobalConstants.YUNJI_ERROR_CODE;
        }
        statisticsDetail.addBusiness(logicCode);
    }

    private void doFailed(Exception ex, DubboService dubboService) {
        StatisticsDetail statisticsDetail = collectMap.get(dubboService.getId());
        statisticsDetail.addStatusCode(GlobalConstants.HTTP_ERROR_CODE);
        CollectorUtils.collectErrorResponse(openAgentCollectQps,
                dubboService.getServiceName() + ":" + dubboService.getMethodName(), null);

    }

    //-----------------   2020.07.02  dubbo 也要支持海量参数 -----------------

    private ExecutorService executorService = Executors.newFixedThreadPool(1000, new NamedThreadFactory("DubboParamFetch-", false));


    private Map<Integer, BlockingQueue<List<String>>> paramsBlockingQueueMap;
    /**
     * 各链路分块大小(根据各链路的QPS决定),key:bulletId,value:参数块大小.
     */
    private Map<Integer, Integer> linkLumpSizeMap;


    private void initDubboParamsTask() throws InterruptedException {
        log.info("[DUBBO] Init Dubbo params Data, 开始初始化所有DUBBO链路的参数");
        for (Bullet bullet : task.getBullets()) {
            loopParamFetchTask(bullet.getId());
        }
        paramSetoutLatch.await(20, TimeUnit.MINUTES);
        log.info("[DUBBO] 所有链路参数获取Task,初始化完毕,数量: {}.", task.getBullets().size());

    }

    protected void loopParamFetchTask(Integer linkId) {
        ParamTransmit paramTransmit = task.getParamTransmit();
        log.info("{}-linkId:{}.LoopParamFetchTask,paramTransmit:{}", getClass().getSimpleName(), linkId, paramTransmit);
        executorService.execute(() -> {
            //paramTransmit
            if (ORDERS == paramTransmit) {
                doOrdersParamFetch(linkId);
            } else {
                log.info("Dubbo paramTransmit: {}.", paramTransmit);
                doParamSelfFetch(linkId);
            }
        });
    }


    //TODO DUBBO 参数范围获取.
    private void doOrdersParamFetch(Integer linkId) {
        BlockingQueue<List<String>> queue = paramsBlockingQueueMap.get(linkId);
        Bullet bullet = bulletIdMap.get(linkId);

        Integer lumpSize = this.linkLumpSizeMap.get(bullet.getId());
        ParamRange range = bullet.getParamRange();
        info("[DUBBO]: 链路{},param orders模式,参数区间 {}-{},lumpSize:{}.", linkId, range.getStart(), range.getEnd(), lumpSize);

        if (range.isEmpty() || lumpSize == 0) {
            info("链路{}(transmit orders)参数为空,或者分摊的QPS为0,detail:range:{},lumpSize:{}",
                    linkId, range.toString(), lumpSize);
            paramSetoutLatch.countDown();
            return;
        }

        if (range.size() <= PARAM_TRANSMIT_BOUNDARY) {
            changeParamFetcherToSelfMode(bullet);
            return;
        }

        int selectedCount = 1, n = 1;

        int randomReuseCount = 1;

        do {
            try {
                if (!runFlag.get()) {
                    info("[DUBBO] LinkId: {} 压测停止，总共生产了{}个批次", linkId, selectedCount - 1);
                    break;
                }

                List<String> params = paramsService.selectParamsByRange(linkId, n * lumpSize > range.size() ?
                        //最后一轮,要获取的参数已经超过了给定的参数的最大值.
                        range.subRange((n - 1) * lumpSize, range.size()) :
                        // (n-1)*lumpSize - n*lumpSize
                        range.subRange((n - 1) * lumpSize, n * lumpSize));

                //将查询出来的参数放近队列中,队列默认只有4个容量,所以最多存放4个lump参数块。
                if (queue.offer(params, 5, TimeUnit.SECONDS)) {
                    if (queue.size() == 1) {
                        paramSetoutLatch.countDown();
                    }
                    // 插入成功才需要移位
                    selectedCount++;

                    if (bullet.getParamMode() == ParamMode.RANDOM) {
                        //参数总数/块的大小+1 = 取块的大小时的最大的块数.
                        //例如5-10003，一共9998个参数,每个参数块121，因此批次为 82*121 + 77，需要82+1=83个批次.
                        int maxLoopSize = (int) Math.ceil(range.size() / (lumpSize * 1.0) + 1);
                        //主要是判断当前要取的块,在总的参数中的下标位置,如果下标已经为0了,说明要复位，重新从最开始取值.
                        int currentLoopIndex = selectedCount % maxLoopSize;
                        // 循环使用
                        if (currentLoopIndex == 0) {
                            randomReuseCount++;
                            info("链路ID:{},ParamMode:{},参数循环使用,进入第{}轮,lumpSize:{},批次:{}.",
                                    linkId, bullet.getParamMode(), randomReuseCount, lumpSize, selectedCount - 1);
                        }
                        n = (currentLoopIndex == 0 ? 1 : currentLoopIndex);
                    } else {
                        //顺序使用,如果达到最大值，也不会从头取获取参数了.
                        n = selectedCount;
                    }
                    //这种情况在不可重复使用参数消费(顺序消费ORDER)情况下发生,不再继续消费了
                    if ((n - 1) * lumpSize >= range.size()) {
                        info("链路ID:{},ParamMode:{}所有数据已经生产完成。数据总量:{},参数块:{},生产批次:{}.",
                                linkId, bullet.getParamMode(), range.size(), lumpSize, selectedCount - 1);
                    }
                } else {
                    log.warn("链路{}参数插入队列数据超时,队列长度为：{}", linkId, queue.size());
                    Thread.sleep(10); // 释放cpu执行权限
                }
            } catch (Exception e) {
                log.error("链路 {} 获取参数数据失败" + e.getMessage(), linkId, e);
            }
        } while ((n - 1) * lumpSize < range.size());
        info("链路 {} 参数生产完毕,退出 While Loop,n:{},selectedCount:{}.", linkId, n, selectedCount);
    }

    private void changeParamFetcherToSelfMode(Bullet bullet) {
        Integer linkId = bullet.getId();
        info("链路{}参数小于{},采用直接获取参数模式(PARAM_SELF)", linkId, PARAM_TRANSMIT_BOUNDARY);
        doParamSelfFetch(linkId);
        bullet.setTransmit(PARAM_SELF);
        //释放锁.
        paramSetoutLatch.countDown();
    }

    /**
     * 根据链路id一次性查出所有的参数.
     */
    private void doParamSelfFetch(Integer linkId) {
        Bullet bullet = bulletIdMap.get(linkId);
        List<String> params = paramsService.selectParamsByLinkId(linkId);
        bullet.setParams(params);
    }

    private String getParam0(Bullet bullet) {
        ParamTransmit transmit = task.getParamTransmit();
        if (bullet.getTransmit() != null) {
            transmit = bullet.getTransmit();
        }
        switch (transmit) {
            case ORDERS:
                ParamRange paramRange = bullet.getParamRange();
                if (paramRange.size() == 0) {
                    return null;
                }
                return getParamOfIdsOrOrders(bullet);

            default:
                return getParamOfSelfParams(bullet);
        }
    }

    private String getParamOfIdsOrOrders(Bullet bullet) {
        String param = null;
        try {
            if (bullet.getCurrent() == 0 || bullet.getCurrent() == this.linkLumpSizeMap.get(bullet.getId())) { // 没有
                List<String> pollParams = this.paramsBlockingQueueMap.get(bullet.getId()).poll(10, TimeUnit.MILLISECONDS);
                bullet.setParams(pollParams);
                bullet.setCurrent(0);
            }
            if (bullet.getParamMode() == ParamMode.ORDER) {
                if (bullet.getCurrent() == bullet.getParams().size()) { // 如果已经没有数据了则直接返回。
                    return null;
                }
            }
            List<String> params = bullet.getParams();
            param = bullet.getParamMode() == ParamMode.ORDER ? params.get(bullet.getCurrent()) : randomParam(params);
            bullet.setCurrent(bullet.getCurrent() + 1);
        } catch (Exception e) {
            log.error("GetParam got error,cause: " + e.getMessage(), e);
        }
        return param;
    }

    private String getParamOfSelfParams(Bullet bullet) {
        return randomParam(bullet.getParams());
    }
}
