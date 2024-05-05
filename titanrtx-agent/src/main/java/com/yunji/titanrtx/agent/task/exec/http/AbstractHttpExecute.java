package com.yunji.titanrtx.agent.task.exec.http;

import com.yunji.titanrtx.agent.collect.CollectScheduler;
import com.yunji.titanrtx.agent.future.core.HttpFutureCallBack;
import com.yunji.titanrtx.agent.future.fatory.HttpFutureObjectFactory;
import com.yunji.titanrtx.agent.service.LiaisonService;
import com.yunji.titanrtx.agent.service.ParamsService;
import com.yunji.titanrtx.agent.task.exec.AbstractExecute;
import com.yunji.titanrtx.common.domain.task.*;
import com.yunji.titanrtx.common.enums.ParamMode;
import com.yunji.titanrtx.common.enums.ParamTransmit;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import com.yunji.titanrtx.common.u.CollectionU;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.common.u.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObjectFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.yunji.titanrtx.common.GlobalConstants.PARAM_TRANSMIT_BOUNDARY;
import static com.yunji.titanrtx.common.enums.ParamTransmit.*;

/**
 * @author Denim.leihz 2019-11-09 2:22 PM
 */
@Slf4j
public abstract class AbstractHttpExecute extends AbstractExecute<HttpFutureCallBack> {
    //todo 限制链路上限.
    /**
     * 最高1000个链路并发压测.
     */
    private static final int MAX_CONCURRENT = 1000;

    private ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT, new NamedThreadFactory("ParamFetch-", false));
    /**
     * 各链路参数数据块,key为链路id,value为链路参数数据块.
     */
    private Map<Integer, BlockingQueue<List<String>>> paramsBlockingQueueMap;
    /**
     * 各链路分块大小(根据各链路的QPS决定),key:bulletId,value:参数块大小.
     */
    private Map<Integer, Integer> linkLumpSizeMap;


    public AbstractHttpExecute(TaskDispatcher taskDispatcher,
                               LiaisonService liaisonService,
                               Task task,
                               CollectScheduler collectScheduler,
                               ParamsService paramsService) {
        super(taskDispatcher, liaisonService, task, collectScheduler, paramsService);
    }

    @Override
    protected PooledObjectFactory<HttpFutureCallBack> objectFactory() {
        return new HttpFutureObjectFactory(openAgentCollectQps);
    }

    protected void detailTaskInit() {
        log.info("AbstractHttpExecute detailTaskInit is initiation.");
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
            log.info("链路:{} 的weight={} 所占用的qps为：{}", bullet.getId(), bullet.getWeight(), qps);
            this.linkLumpSizeMap.put(linkId, qps);
        }

    }


    protected void doPrepareExecute() throws InterruptedException {
        prepareHttpClient();
        for (Bullet bullet : task.getBullets()) {
            HttpLink httpLink = (HttpLink) bullet;
            try {
                String host = new URL(CommonU.buildFullUrl(httpLink.getProtocol().getMemo(), httpLink.getUrl())).getHost();
                hostMap.putIfAbsent(httpLink.getId(), host);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                runFlag.set(false);
            }
        }
        //参数获取初始化
        initLinkParamsTask();
    }

    private void initLinkParamsTask() throws InterruptedException {
        log.info("InitParamsData,开始初始化所有链路的参数");
        for (Bullet bullet : task.getBullets()) {
            loopParamFetchTask(bullet.getId());
        }
        paramSetoutLatch.await(20, TimeUnit.MINUTES);
        log.info("所有链路参数获取Task,初始化完毕,数量: {}.", task.getBullets().size());
    }

    /**
     * 创建线程任务,根据链路id为当前链路增量的准备好参数,供压测使用.
     *
     * @param linkId 链路id
     */

    protected void loopParamFetchTask(Integer linkId) {
        ParamTransmit paramTransmit = task.getParamTransmit();
        log.info("{}-linkId:{}.LoopParamFetchTask,paramTransmit:{}", getClass().getSimpleName(), linkId, paramTransmit);
        executorService.execute(() -> {
            //paramTransmit
            if (IDS == paramTransmit) {
                doIDParamFetch(linkId);
            } else if (ORDERS == paramTransmit) {
                doOrdersParamFetch(linkId);
            } else {
                assert paramTransmit == PARAM_SELF;
                doParamSelfFetch(linkId);
            }
        });
    }


    @Override
    protected void doClose() {
        executorService.shutdown();
    }

    protected abstract void prepareHttpClient();


    private void doIDParamFetch(Integer linkId) {
        BlockingQueue<List<String>> queue = paramsBlockingQueueMap.get(linkId);
        Bullet bullet = bulletIdMap.get(linkId);
        Integer lumpSize = this.linkLumpSizeMap.get(bullet.getId());

        List<Integer> paramIds = bullet.getParamIds();
        if (CollectionU.isEmpty(paramIds) || lumpSize == 0) {
            info("链路{}(transmit ids)参数为空,或者分摊的QPS为0,detail:ids size:{},lumpSize:{}",
                    linkId, paramIds.size(), lumpSize);
            paramSetoutLatch.countDown();
            return;
        }
        if (paramIds.size() <= PARAM_TRANSMIT_BOUNDARY) {
            changeParamFetcherToSelfMode(bullet);
            return;
        }

        //当前生产的批次,1开始
        int selectedCount = 1;
        //从已给的参数范围中,按照lumpSize一块一块进行选择。
        //例如总数1000,lumpSize 90
        //1. 90*0 - 90*1
        //2. 90*1 - 90*2
        //3. 90*2 - 90*3
        int n = 1;
        int randomReuseCount = 1;
        do {
            try {
                if (!runFlag.get()) {
                    info("LinkId: {} 压测停止，总共生产了{}个批次", linkId, selectedCount - 1);
                    break;
                }

                List<String> params = paramsService.selectParamsBatch(n * lumpSize > paramIds.size() ?
                        //最后一轮,要获取的参数已经超过了给定的参数的最大值
                        paramIds.subList((n - 1) * lumpSize, paramIds.size()) :
                        // (n-1)*lumpSize - n*lumpSize
                        paramIds.subList((n - 1) * lumpSize, n * lumpSize));

                //将查询出来的参数放近队列中,队列默认只有4个容量,所以最多存放4个lump参数块。
                if (queue.offer(params, 5, TimeUnit.SECONDS)) {
                    if (queue.size() == 3) {
                        paramSetoutLatch.countDown();
                    }
                    // 插入成功才需要移位
                    selectedCount++;

                    if (bullet.getParamMode() == ParamMode.RANDOM) {
                        //参数总数/块的大小+1 = 取块的大小时的最大的块数.
                        int maxLoopSize = (int) Math.ceil(paramIds.size() / (lumpSize * 1.0) + 1);
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
                    if ((n - 1) * lumpSize >= paramIds.size()) {
                        info("任务ID:{},数据总量:{},ParamMode:{},所有数据已经生产完成 总共生产了{}个批次",
                                linkId, paramIds.size(), bullet.getParamMode(), selectedCount - 1);
                    }
                } else {
                    warn("链路{}参数插入队列数据超时,队列长度为：{}", linkId, queue.size());
                    Thread.sleep(10); // 释放cpu执行权限
                }
            } catch (Exception e) {
                log.error("链路 {} 获取参数数据失败" + e.getMessage(), linkId, e);
            }
        } while ((n - 1) * lumpSize < paramIds.size());
    }

    //TODO,在commander 已经分段的问题. 已完成. commander 需要注意右区间
    //TODO,循环使用和顺序使用问题.
    private void doOrdersParamFetch(Integer linkId) {
        BlockingQueue<List<String>> queue = paramsBlockingQueueMap.get(linkId);
        Bullet bullet = bulletIdMap.get(linkId);

        Integer lumpSize = this.linkLumpSizeMap.get(bullet.getId());
        ParamRange range = bullet.getParamRange();
        info("链路{},param orders模式,参数区间 {}-{},lumpSize:{}.", linkId, range.getStart(), range.getEnd(), lumpSize);

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
                    info("LinkId: {} 压测停止，总共生产了{}个批次", linkId, selectedCount - 1);
                    break;
                }

                List<String> params = paramsService.selectParamsByRange(linkId, n * lumpSize > range.size() ?
                        //最后一轮,要获取的参数已经超过了给定的参数的最大值.
                        range.subRange((n - 1) * lumpSize, range.size()) :
                        // (n-1)*lumpSize - n*lumpSize
                        range.subRange((n - 1) * lumpSize, n * lumpSize));

                //将查询出来的参数放近队列中,队列默认只有4个容量,所以最多存放4个lump参数块。
                if (queue.offer(params, 5, TimeUnit.SECONDS)) {
                    if (queue.size() == 3) {
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
                                    linkId, randomReuseCount, lumpSize, selectedCount - 1);
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
                    warn("链路{}参数插入队列数据超时,队列长度为：{}", linkId, queue.size());
                    Thread.sleep(10); // 释放cpu执行权限
                }
            } catch (Exception e) {
                log.error("链路 {} 获取参数数据失败" + e.getMessage(), linkId, e);
            }
        } while ((n - 1) * lumpSize < range.size());
        info("链路 {} 参数生产完毕,退出 While Loop,n:{},selectedCount:{}.", linkId, n, selectedCount);
    }

    /**
     * 根据链路id一次性查出所有的参数.
     */
    private void doParamSelfFetch(Integer linkId) {
        Bullet bullet = bulletIdMap.get(linkId);
        List<String> params = paramsService.selectParamsByLinkId(linkId);
        bullet.setParams(params);
    }

    /**
     * 顺序获取参数,容易造成 为 null 的情况
     */
    protected String getParam(Bullet bullet) {
        ParamTransmit transmit = task.getParamTransmit();
        if (bullet.getTransmit() != null) {
            transmit = bullet.getTransmit();
        }
        switch (transmit) {
            case IDS:
                List<Integer> paramIds = bullet.getParamIds();
                if (CollectionU.isEmpty(paramIds)) {
                    return null;
                }
                return getParamOfIdsOrOrders(bullet);
            case ORDERS:
                ParamRange paramRange = bullet.getParamRange();
                if (paramRange.size() == 0) {
                    return null;
                }
                return getParamOfIdsOrOrders(bullet);
            case PARAM_SELF:
            default:
                return getParamOfSelfParams(bullet);
        }
    }


    private String getParamOfIdsOrOrders(Bullet bullet) {
        String param = null;
        try {
            //concurrent = 0 是第一次进来的情况
            if (bullet.getCurrent() == 0) {
                log.info("linkId:{}: GetParam by ({}) current 为0,第一次进来获取参数.", bullet.getId(), bullet.getParamMode());
                fetchParamsOfIdOrOrders(bullet);
            }
            //当前轮拉取下来的参数被消费完了，需要拉取新一批的参数了
            if (bullet.getCurrent() == this.linkLumpSizeMap.get(bullet.getId())) {
                fetchParamsOfIdOrOrders(bullet);
            }
            //如果是顺序消费,参数只使用一轮,则当获取到的参数为null时,结束
            if (bullet.getParamMode() == ParamMode.ORDER) {
                //bullet.getParams().size() 不及 current，说明参数补充完了,如果已经没有数据了则直接返回。
                if (bullet.getParams() == null || bullet.getCurrent() == bullet.getParams().size()) {
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

    //补充参数
    private void fetchParamsOfIdOrOrders(Bullet bullet) throws InterruptedException {
        List<String> pollParams = this.paramsBlockingQueueMap.get(bullet.getId()).poll(10, TimeUnit.MILLISECONDS);
        if (pollParams == null) {
            log.warn("When fetchParamsOfIdOrOrders poll params is null.");
        }
        bullet.setParams(pollParams);
        bullet.setCurrent(0);
    }

    private String getParamOfSelfParams(Bullet bullet) {
        return randomParam(bullet.getParams());
    }


    private void changeParamFetcherToSelfMode(Bullet bullet) {
        Integer linkId = bullet.getId();
        info("链路{}参数小于{},采用直接获取参数模式(PARAM_SELF)", linkId, PARAM_TRANSMIT_BOUNDARY);
        doParamSelfFetch(linkId);
        bullet.setTransmit(PARAM_SELF);
        //释放锁.
        paramSetoutLatch.countDown();
    }

    private void info(String format, Object... arguments) {
        String logPrefix = "[" + Thread.currentThread().getName() + "]";
        log.info(logPrefix + format, arguments);
    }

    private void warn(String format, Object... arguments) {
        String logPrefix = "[" + Thread.currentThread().getName() + "]";
        log.warn(logPrefix + format, arguments);
    }

}
