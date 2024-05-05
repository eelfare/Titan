package com.yunji.titanrtx.commander.service.impl.dubbo;

import com.yunji.titanrtx.commander.service.LiaisonService;
import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.common.service.AgentReportService;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import javax.annotation.Resource;

@Slf4j
public class AgentReportServiceImpl implements AgentReportService {

    @Resource
    private TaskDispatcher taskDispatcher;

    @Resource
    private LiaisonService liaisonService;

    @Override
    public void report(Statistics statistics) {
        log.info("接收到任务完成上报 taskNo:{},type:{},address:{}.............................",statistics.getTaskNo(),statistics.getTaskType(),statistics.getAddress());
        InterProcessMutex lock = null;
        try {
            lock = liaisonService.acquireTaskReportLock(statistics.getTaskNo());
            taskDispatcher.report(statistics);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (lock != null){
                    lock.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
