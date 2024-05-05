package com.yunji.titanrtx.commander.service.impl.task;

import com.yunji.titanrtx.commander.service.LiaisonService;
import com.yunji.titanrtx.commander.support.flow.Flow;
import com.yunji.titanrtx.common.domain.meta.AgentMeta;
import com.yunji.titanrtx.common.domain.progress.Progress;
import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.common.domain.statistics.StatisticsDetail;
import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.ParamRange;
import com.yunji.titanrtx.common.domain.task.Task;
import com.yunji.titanrtx.common.enums.AgentStatus;
import com.yunji.titanrtx.common.enums.ParamTransmit;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.AgentService;
import com.yunji.titanrtx.common.service.CommanderReportService;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import com.yunji.titanrtx.common.u.CloneUtils;
import com.yunji.titanrtx.common.u.CollectionU;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.common.u.LogU;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.yunji.titanrtx.common.GlobalConstants.PARAM_TRANSMIT_BOUNDARY;

@Slf4j
@Service
public class CommanderTaskDispatcher implements TaskDispatcher {

    @Resource
    protected LiaisonService liaisonService;

    @Resource
    protected AgentService agentService;

    @Resource
    protected CommanderReportService commanderReportService;


    @Override
    public RespMsg start(Task task) {

        long concurrent = task.getConcurrent();
        List<String> availableAgent = getAvailableAgent();

        long singleMachineConcurrent = task.getSingleMachineConcurrent();
        String taskNo = task.getTaskNo();

        List<String> taskAgents = Flow.doSelect(task.getFlow(), availableAgent, concurrent, singleMachineConcurrent);

        long decreaseConcurrent = task.getConcurrent();
        long decreaseTotal = task.getTotal();

        long averageConcurrent = decreaseConcurrent / taskAgents.size();
        long averageTotal = decreaseTotal / taskAgents.size();

        if (averageTotal > Integer.MAX_VALUE) averageTotal = Integer.MAX_VALUE;
        if (averageConcurrent > Integer.MAX_VALUE) averageConcurrent = Integer.MAX_VALUE;

        /*
         * 设置栅栏
         */
        liaisonService.setBarrier(task.getTaskNo());
        final CountDownLatch latch = new CountDownLatch(taskAgents.size());
        AtomicBoolean isSuccess = new AtomicBoolean(true);
        try {
            for (int i = 0; i < taskAgents.size(); i++) {

                if (!isSuccess.get()) {
                    fastFail(taskNo, taskAgents);
                    return RespMsg.respErr();
                }
                Task agentTask = CloneUtils.clone(task); // 深拷贝
                if (i == (taskAgents.size() - 1)) {
                    if (decreaseConcurrent > Integer.MAX_VALUE) decreaseConcurrent = Integer.MAX_VALUE;
                    if (decreaseTotal > Integer.MAX_VALUE) decreaseTotal = Integer.MAX_VALUE;
                    agentTask.setConcurrent(decreaseConcurrent);
                    agentTask.setTotal(decreaseTotal);
                } else {
                    agentTask.setConcurrent(averageConcurrent);
                    agentTask.setTotal(averageTotal);
                    decreaseConcurrent -= averageConcurrent;
                    decreaseTotal -= averageTotal;
                }

                switch (agentTask.getParamTransmit()) {
                    case IDS:
                        paramsIdAllot(agentTask, i, taskAgents.size());
                        break;
                    case ORDERS:
                        paramIdRangeAllot(agentTask, i, taskAgents.size());
                        break;
                    case PARAM_SELF:
                    default:
                        break;
                }
                String agent = taskAgents.get(i);
                log.info("[{}]准备下发任务到agent:{},参数下发模式:{},agent总数:{}.",
                        agentTask.getTaskType(), agent, agentTask.getParamTransmit(), taskAgents.size());
                //下发任务到agent节点
                liaisonService.ensureRemoteDomain(agent);
                agentService.start(agentTask);
                CompletableFuture<RespMsg> future = RpcContext.getContext().getCompletableFuture();
                future.whenComplete((respMsg, exception) -> {
                    if (exception != null) {
                        exception.printStackTrace();
                        isSuccess.set(false);
                        latch.countDown();
                    } else {
                        latch.countDown();
                        log.info("任务下发agent节点成功:taskNo:{},agent:{}.........................................", taskNo, agent);
                    }
                });
            }
            latch.await(20, TimeUnit.MINUTES);
            if (latch.getCount() != 0 || !isSuccess.get()) {
                fastFail(taskNo, taskAgents);
                return RespMsg.respErr();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fastFail(taskNo, taskAgents);
            return RespMsg.respErr("任务下发agent节点发生异常,agent:" + taskAgents);
        }
        if (isSuccess.get()) {
            liaisonService.savePressureAgent(task.getTaskType(), task.getId(), taskAgents);
            doOpenBarrier(task.getTaskNo());
            return RespMsg.respSuc(taskNo);
        }
        return RespMsg.respErr();
    }

    /**
     * 任务的参数分配
     * 将有序参数均分给n个agent.
     *
     * @param target            任务目标
     * @param currentAgentIndex 当前分配agent的位置
     * @param totalAgent        所有的agent数量
     */
    private void paramsIdAllot(Task target, int currentAgentIndex, int totalAgent) {
        List<Bullet> bullets = target.getBullets();
        for (Bullet bullet : bullets) {
            List<Integer> paramIds = bullet.getParamIds();
            int countParamIds = paramIds.size();
            //小于1000的参数,就不再均分了.
            if (countParamIds < 1000) {
                continue;
            }
            // 平均每台agent需要分配的参数个数
            int averageParamIds = countParamIds / totalAgent;
            List<Integer> tempList = paramIds.subList(currentAgentIndex * averageParamIds,
                    currentAgentIndex != totalAgent - 1
                            ? currentAgentIndex * averageParamIds + averageParamIds
                            : countParamIds);
            // 设置参数起始位置
            bullet.setParamIds(tempList);
            log.info("Agent index:{},Bullet:{},allot param ids mode.", currentAgentIndex, bullet.getId());
        }
    }

    private void paramIdRangeAllot(Task target, int currentAgentIndex, int totalAgent) {
        List<Bullet> bullets = target.getBullets();
        for (Bullet bullet : bullets) {
            ParamRange paramRange = bullet.getParamRange();
            if (paramRange == null) {
                continue;
            }
            int paramSize = paramRange.size();
            //小于 2000 的参数,就不再均分了.
            if (paramSize < PARAM_TRANSMIT_BOUNDARY) {
                continue;
            }
            // 平均每台agent需要分配的参数个数
            int paramSizePerAgent = paramSize / totalAgent;
            //不是最后一个agent.
            ParamRange subRange;
            if (currentAgentIndex != totalAgent - 1) {
                subRange = new ParamRange(currentAgentIndex * paramSizePerAgent, (currentAgentIndex + 1) * paramSizePerAgent);
            } else {
                //+1？因为range右边为开区间,如果最终需要最后一个参数时,我们要+1.
                subRange = new ParamRange(currentAgentIndex * paramSizePerAgent, paramSize + 1);
            }
            // 设置参数起始位置
            bullet.setParamRange(subRange);
            log.info("Agent index:{},Bullet:{},allot param orders mode,range:{}",
                    currentAgentIndex, bullet.getId(), subRange);
        }
    }

    private void doOpenBarrier(String taskNo) {
        /*
         * 设置当前任务完成上报的分布式锁(分布式锁采用懒删除策略，当commander重启时删除)
         * 打开栅栏
         */
        liaisonService.hookTaskReportLock(taskNo);
        liaisonService.openBarrier(taskNo);
    }


    @Override
    public synchronized void report(Statistics statistics) {
        String taskNo = statistics.getTaskNo();
        Integer id = CommonU.parseTaskNoToId(taskNo);
        TaskType type = statistics.getTaskType();
        /*
         *从redis里面获取执行任务的agent   如果为空则已经上报完毕，每次上报删除对应的agent节点
         */
        List<String> agents = liaisonService.queryPressureAgent(type, id);
        /*
         * 防止多次上报 幂等次
         */
        if (CollectionU.isEmpty(agents)) return;
        boolean isExist = agents.remove(statistics.getAddress());
        if (!isExist) return;

        Statistics preStatistics = liaisonService.queryPressureStatistics(taskNo);
        if (preStatistics == null) {
            preStatistics = statistics;
        } else {
            fuse(preStatistics, statistics);
        }
        /*
         * 如果已经不存在未上报的agent 则继续向manager上报，否则存入redis
         */
        if (agents.size() == 0) {
            log.info("统计结果完成，taskNo:{},详情见detail日志........................................................", taskNo);
            LogU.info("统计结果完成，taskNo:{},stat:{}........................................................", taskNo, preStatistics);
            RpcContext.getContext().removeAttachment(Constants.REMOTE_DOMAIN);
            commanderReportService.report(preStatistics);
            liaisonService.deletePressureAgent(type, id);
            liaisonService.deletedPressureStatistics(taskNo);
            //todo 需要解锁吗? 2020.08.29
            liaisonService.cancelTaskReportLock(taskNo);
        } else {
            liaisonService.savePressureAgent(type, id, agents);
            liaisonService.savePressureStatistics(preStatistics);
        }
    }


    private void fuse(Statistics preStatistics, Statistics reportStatistics) {
        preStatistics.setRequestTotal(preStatistics.getRequestTotal() + reportStatistics.getRequestTotal());
        Map<Integer, StatisticsDetail> preDetailMap = preStatistics.getDetailMap();
        reportStatistics.getDetailMap().forEach((integer, statisticsDetail) -> {
            StatisticsDetail preDetail = preDetailMap.get(integer);
            if (preDetail == null) {
                preDetailMap.put(integer, statisticsDetail);
            } else {
                preDetail.setDuration(preDetail.getDuration() + statisticsDetail.getDuration());
                preDetail.addAll(preDetail.getSTATUS_CODE_MAP(), statisticsDetail.getSTATUS_CODE_MAP());
                preDetail.addAll(preDetail.getBUSINESS_CODE_MAP(), statisticsDetail.getBUSINESS_CODE_MAP());
            }
        });
        preStatistics.setEndTime(reportStatistics.getEndTime());
    }


    private void fastFail(String taskNo, List<String> taskAgents) {
        doStop(taskAgents);
        log.error("下发任务时异常进入回退taskAgents:{}................................", taskAgents);
        liaisonService.openBarrier(taskNo);
    }

    private void doStop(List<String> agentAddress) {
        CountDownLatch latch = new CountDownLatch(agentAddress.size());
        for (String address : agentAddress) {
            try {
                liaisonService.ensureRemoteDomain(address);
                agentService.stop();
                CompletableFuture<RespMsg> helloFuture = RpcContext.getContext().getCompletableFuture();
                helloFuture.whenComplete((respMsg, exception) -> {
                    if (exception != null) {
                        exception.printStackTrace();
                    } else {
                        log.info("停止任务成功，agent:{}................................", address);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }
        try {
            latch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public RespMsg progress() {
        List<String> agents = liaisonService.queryPressureAgent();
        if (CollectionU.isEmpty(agents)) return RespMsg.respSuc();
        List<Progress> progressMetas = new ArrayList<>(agents.size());
        for (String agent : agents) {
            liaisonService.ensureRemoteDomain(agent);
            Progress progress = (Progress) agentService.progress().getData();
            if (progress != null) {
                progressMetas.add(progress);
                log.info("接收到节点进度信息，progress:{}......................................", progress);
            }
        }
        return RespMsg.respSuc(progressMetas);
    }


    private List<String> getAvailableAgent() {
        List<AgentMeta> metas = liaisonService.agentMetas();
        List<String> availableAgent = new ArrayList<>();
        for (AgentMeta meta : metas) {
            AgentStatus status = meta.getAgentStatus();
            if (AgentStatus.IDLE == status) {
                availableAgent.add(meta.getAddress());
            }
        }
        return availableAgent;
    }


    @Override
    public RespMsg stop() {
        doStop(liaisonService.queryPressureAgent());
        return RespMsg.respSuc();
    }
}
