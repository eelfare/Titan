package com.yunji.titanrtx.agent.task.exec;

import com.google.common.util.concurrent.RateLimiter;
import com.yunji.titanrtx.agent.boot.AgentRegister;
import com.yunji.titanrtx.agent.collect.CollectScheduler;
import com.yunji.titanrtx.agent.service.LiaisonService;
import com.yunji.titanrtx.agent.service.ParamsService;
import com.yunji.titanrtx.agent.strategy.Strategy;
import com.yunji.titanrtx.agent.task.Execute;
import com.yunji.titanrtx.agent.task.InternetRepairThread;
import com.yunji.titanrtx.agent.task.TimeOutThread;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.progress.Progress;
import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.common.domain.statistics.StatisticsDetail;
import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.Task;
import com.yunji.titanrtx.common.enums.*;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import com.yunji.titanrtx.common.u.CollectionU;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class AbstractExecute<T> implements Execute {
    private AtomicBoolean stopped = new AtomicBoolean(false);

    private TaskDispatcher taskDispatcher;

    private LiaisonService liaisonService;
    /**
     * 链路参数块容量.eg.一个链路的参数块的数量,至少为2,保证压测有流量.
     */
    protected static final int ORDER_QUEUE_CAPACITY = 4;

    protected Task task;

    protected List<Integer> requestIds;

    protected CountDownLatch countDownLatch;
    // 所有链路数据准备情况
    protected CountDownLatch paramSetoutLatch;

    private Strategy strategy;
    /**
     * bulletId与bullet的映射.
     */
    protected Map<Integer, Bullet> bulletIdMap;
    /**
     * 对HTTP才会用到
     * 存储去除域名后的url的map,例如: origin url: http://wwww.baidu.com/app/getApp.json
     * urlIdMap 里面则会存储 1 -> /app/getApp.json
     */
    protected Map<Integer, String> urlIdMap;

    protected ParamsService paramsService;

    private TimeOutThread timeOutThread;

    private AtomicInteger atomRequestTimes = new AtomicInteger(0);

    protected AtomicBoolean runFlag = new AtomicBoolean(true);

    AtomicBoolean collectFlag = new AtomicBoolean(false);

    protected GenericObjectPool<T> objectPool;

    @Getter
    public Map<Integer, StatisticsDetail> collectMap = new ConcurrentHashMap<>();

    private Map<Integer, Double> average = new HashMap<>();

    protected Map<Integer, String> hostMap = new HashMap<>();

    protected CollectScheduler collectScheduler;

    protected boolean openAgentCollectQps;

    public AbstractExecute(TaskDispatcher taskDispatcher,
                           LiaisonService liaisonService,
                           Task task,
                           CollectScheduler collectScheduler,
                           ParamsService paramsService) {
        this.taskDispatcher = taskDispatcher;
        this.liaisonService = liaisonService;
        this.task = task;
        this.collectScheduler = collectScheduler;
        openAgentCollectQps = collectScheduler != null;
        this.paramsService = paramsService;
    }

    protected void initTask() throws InterruptedException {
        log.info(getClass().getName() + " initTask doing...");
        this.requestIds = requestIds(task);
        countDownLatch = new CountDownLatch((int) task.getTotal());
        strategy = Strategy.doSelect(task.getStrategy(), task.getConcurrent(), task.getTotal());
        this.bulletIdMap = new HashMap<>();

        detailTaskInit();
        configObjectPool();
        doPrepareExecute();
//        initParamsData();
    }

    @Override
    public void run() {
        try {
            log.info("等待栅栏解除................................................................");
            liaisonService.waitOnBarrier(GlobalConstants.BARRIER_PATH + "/" + task.getTaskNo(), 1, TimeUnit.MINUTES);
            log.info("栅栏解除成功..................................................................");
            if (runFlag.get()) {
                setSubsidiary(getType());
                if (collectScheduler != null) {
                    // 开始采集数据定定时任务
                    collectScheduler.start();
                }
                log.info("任务总数: {} ................", task.getTotal());
                long st = System.currentTimeMillis();
                while (atomRequestTimes.get() != task.getTotal()) {
                    if (!runFlag.get()) break;
                    Integer id = requestIds.get(atomRequestTimes.get() % requestIds.size());
                    acquireInvoke(id);
                    doInvoke(bulletIdMap.get(id));
                }
                if (runFlag.get()) {
                    try {
                        log.info("执行压测发送完毕，正在等在响应结束....................................;");
                        Thread.sleep(2000);
//                        countDownLatch.await();
                        log.info("执行压测发送完毕，等待结束,开始数据上报....................................;");
                        runFlag.set(false);
                    } catch (InterruptedException e) {
                        log.error("等待解锁异常:{}............", e.getMessage());
                    }
                }
                log.info("压测完毕,耗时:{} ms ................", (System.currentTimeMillis() - st));
                collect(1);
            }
            doStop();
            doClose();
        } catch (Throwable e) {
            log.error("线程 " + Thread.currentThread().getName() + "出现异常,压测被迫中断,原因: " + e.getMessage(), e);
        } finally {
            if (collectScheduler != null) {
                // 解除采集数据的定时任务
                collectScheduler.stop();
            }
            log.info("[{}]压测任务退出,runFlag:{},atomRequestTimes:{} ", Thread.currentThread().getName(), runFlag.get(), atomRequestTimes.get());
//            log.info("StackTrace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
        }
    }

    private void acquireInvoke(int id) {
        RateLimiter rateLimiter = strategy.doStrategy();
        rateLimiter.acquire();
        atomRequestTimes.incrementAndGet();
        StatisticsDetail collect = collectMap.get(id);
        if (collect == null) {
            collect = new StatisticsDetail();
            collectMap.putIfAbsent(id, collect);
        }
    }


    @Override
    public RespMsg doStop() {
        if (stopped.compareAndSet(false, true)) {
            runFlag.set(false);
            log.info("接收到停止任务请求,待归还锁:{}...............................................", countDownLatch.getCount());
            if (paramSetoutLatch != null) {
                log.info("接收到停止任务请求,待归还 paramLatch:{}...............................................", paramSetoutLatch.getCount());
            }
            long st = System.currentTimeMillis();
                /*while (paramSetoutLatch.getCount() > 0) {
                    paramSetoutLatch.countDown();
                }
                while (countDownLatch.getCount() > 0) {
                    countDownLatch.countDown();
                }*/
            if (timeOutThread != null && !timeOutThread.isInterrupted()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timeOutThread.interrupt();
            }
            if (objectPool != null && !objectPool.isClosed()) {
                objectPool.close();
            }
            liaisonService.updateAgentStatus(AgentStatus.IDLE);

            //以防run线程被打断没有 collect()
            collect(2);
            log.info("Agent stop successful,cost:{} ms..............", (System.currentTimeMillis() - st));
        } else {
            log.info("Execute already stopped,flag:{}", stopped.get());
        }
        return RespMsg.respSuc();
    }

    @Override
    public void report(Statistics statistics) {
        taskDispatcher.report(statistics);
        log.info("执行压测任务完毕,statistics:{}..............................................................", statistics);
    }

    @Override
    public void collect(int type) {
        try {
            if (collectFlag.compareAndSet(false, true)) {
                Statistics bo = new Statistics();
                bo.setTaskType(task.getTaskType());
                bo.setTaskNo(task.getTaskNo());
                bo.setUrlIdMap(urlIdMap);
                bo.setAddress(AgentRegister.AGENT_META.getAddress());
                bo.setRequestTotal(atomRequestTimes.intValue());
                bo.setStartTime(task.getStartTime());
                bo.setEndTime(new Date());
                /*
                 * 网络矫正
                 */
                for (Map.Entry<Integer, Double> entries : average.entrySet()) {
                    StatisticsDetail detail = collectMap.get(entries.getKey());
                    if (detail != null) {
                        long duration = detail.getDuration();
                        long times = detail.successTimes();
                        Double netExpend = entries.getValue();
                        duration -= times * netExpend;
                        detail.setDuration(duration);
                    }
                }
                bo.setDetailMap(collectMap);
                report(bo);
            }
        } catch (Exception e) {
            log.error("AbstractExecute collect,maybe report timeout error: " + e.getMessage(), e);
        }
    }

    private void setSubsidiary(TaskType taskType) {
        if (task.getTimeout() != 0) {
            timeOutThread = new TimeOutThread(task.getTimeout(), this);
            timeOutThread.setName("timeoutThread：" + task.getTimeout());
            timeOutThread.start();
        }
        new InternetRepairThread(runFlag, hostMap, average, taskType).start();
    }

    @Override
    public RespMsg progress() {
        Progress progress = new Progress();
        progress.setRequestTimes(atomRequestTimes.intValue());
        progress.setWaitResponseTimes(countDownLatch.getCount());
        progress.setTotal(task.getTotal());
        return RespMsg.respSuc(progress);
    }


    protected List<Integer> requestIds(Task task) {
        long total = task.getTotal();
        Sequence sequence = task.getSequence();
        List<Bullet> bullets = task.getBullets();
        List<Integer> linkIds = new ArrayList<>();

        int totalWeight = totalWeight(bullets);

        if (total <= totalWeight) {
            for (Bullet bullet : bullets) {
                long weight = bullet.getWeight();
                int size = new BigDecimal(total).divide(new BigDecimal(totalWeight), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(weight)).intValue();
                int linkId = bullet.getId();
                for (int i = 0; i < size; i++) {
                    linkIds.add(linkId);
                }
            }
        } else {
            for (Bullet bullet : bullets) {
                long weight = bullet.getWeight();
                int linkId = bullet.getId();
                for (int i = 0; i < weight; i++) {
                    linkIds.add(linkId);
                }
            }
        }
        if (Sequence.OUT == sequence) {
            Collections.shuffle(linkIds);
        }
        return linkIds;
    }

    protected static int totalWeight(List<Bullet> bullets) {
        int total = 0;
        for (Bullet bullet : bullets) {
            total += bullet.getWeight();
        }
        return total;
    }

    /**
     * 随机获取参数
     */
    protected String randomParam(List<String> params) {
        if (CollectionU.isEmpty(params)) return null;
        return params.get((int) (Math.random() * params.size()));
    }

    protected abstract PooledObjectFactory<T> objectFactory();

    protected abstract void doClose();

    protected abstract void doPrepareExecute() throws InterruptedException;

    protected abstract TaskType getTaskType();

    /**
     * Config 对象连接池.
     */
    protected abstract void configObjectPool();

    protected abstract void detailTaskInit();


}
