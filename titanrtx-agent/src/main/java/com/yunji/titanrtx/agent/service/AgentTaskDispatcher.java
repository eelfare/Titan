package com.yunji.titanrtx.agent.service;

import com.yunji.titanrtx.agent.collect.CollectScheduler;
import com.yunji.titanrtx.agent.collect.QpsCollectScheduler;
import com.yunji.titanrtx.agent.collect.report.QpsCollectServiceImpl;
import com.yunji.titanrtx.agent.collect.report.ReportCollectService;
import com.yunji.titanrtx.agent.task.Execute;
import com.yunji.titanrtx.agent.task.exec.AHCExecuteTask;
import com.yunji.titanrtx.agent.task.exec.DubboExecuteTask;
import com.yunji.titanrtx.agent.task.exec.DubboNewExecuteTask;
import com.yunji.titanrtx.agent.task.exec.HttpExecuteTask;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.common.domain.task.Task;
import com.yunji.titanrtx.common.enums.AgentStatus;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.AgentReportService;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import com.yunji.titanrxt.plugin.influxdb.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Service
public class AgentTaskDispatcher implements TaskDispatcher {

    private volatile Execute execute;

    @Resource
    protected LiaisonService liaisonService;

    @Resource
    private AgentReportService agentReportService;

    @Resource(name = "http")
    private ParamsService linkParamsService;

    @Resource(name = "dubbo")
    private ParamsService serviceParamsService;

    private ReportCollectService qpsCollectService;

    @Resource
    StoreService storeService;
    @Value("${influxDB.rpName}")
    private String rpName;

    // agent qps 采集数据上报的时间间隔
    @Value("${agentReportInterval:10}")
    private int agentReportInterval;

    @Value("${async.http.enable:true}")
    private boolean asyncHttpEnable;

    // 是否开启qps采集
    @Value("${open.agent.collect.qps:true}")
    private boolean openAgentCollectQps;
    // 是否开启QPS数据上报
    @Value("${report.qps:true}")
    private boolean reportQps;

    @Value("${ahc.max.connection:5000}")
    private int maxConnection;

    private ExecutorService taskExecutor = Executors.newFixedThreadPool(5,
            new NamedThreadFactory("AgentTaskPool", true));


    @PostConstruct
    public void init() {
        if (!storeService.databaseExists(GlobalConstants.AGENTQPS_COLLECT_QPS_BD_NAME)) {
            storeService.createDatabase(GlobalConstants.AGENTQPS_COLLECT_QPS_BD_NAME);
        }
        storeService.createRetentionPolicy(rpName, GlobalConstants.AGENTQPS_COLLECT_QPS_BD_NAME, GlobalConstants.AGENTQPS_SHARD_DURATION, 1, true);
        qpsCollectService = new QpsCollectServiceImpl(reportQps, storeService, rpName, agentReportInterval);
    }

    @Override
    public RespMsg start(Task task) throws InterruptedException {
        stop();
        TaskType taskType = task.getTaskType();
        String threadName = "";
        // 创建数据采集任务
        CollectScheduler collectScheduler = null;
        if (openAgentCollectQps) {
            collectScheduler = new QpsCollectScheduler(2, agentReportInterval, qpsCollectService);
        }
        // 因为数据在压测之前需要进行初始化，可能时间较长,这里提前把agent机器标识正在运行中
        liaisonService.updateAgentStatus(AgentStatus.RUNNING);
        try {
            switch (taskType) {
                case HTTP:
                    if (asyncHttpEnable) {
                        execute = new AHCExecuteTask(this, liaisonService, task, collectScheduler, maxConnection, linkParamsService);
                        threadName = "asyncHttpRunThread";
                        log.info("Using Netty AHCExecuteTask for execute.");
                    } else {
                        execute = new HttpExecuteTask(this, liaisonService, task, collectScheduler, linkParamsService);
                        threadName = "httpRunThread";
                        log.info("Using HttpClient HttpExecuteTask for execute.");
                    }
                    break;

                case DUBBO:
//                execute = new DubboExecuteTask(this, liaisonService, task, collectScheduler, serviceParamsService);
                    execute = new DubboNewExecuteTask(this, liaisonService, task, collectScheduler, serviceParamsService);
                    threadName = "DubboRunThread";
                    break;
            }
            log.info("....... Agent Task alert hook:{} .......", task.getAlertHook());
            if (task.getTaskType() == TaskType.DUBBO) {
                new Thread(execute, threadName + ":" + task.getTaskNo()).start();
            } else {
                taskExecutor.execute(execute);
            }
        } catch (Exception ex) {
            log.error("Agent start stress execute error: " + ex.getMessage(), ex);
            liaisonService.updateAgentStatus(AgentStatus.IDLE);
        }
        return RespMsg.respSuc();
    }


    @Override
    public RespMsg progress() {
        return execute.progress();
    }

    @Override
    public void report(Statistics statistics) {
        agentReportService.report(statistics);
    }

    @Override
    public RespMsg stop() {
        if (execute != null) {
            return execute.doStop();
        }
        return RespMsg.respSuc();

    }
}
