package com.yunji.titanrtx.commander.service;

import com.yunji.titanrtx.common.domain.meta.AgentMeta;
import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.common.enums.TaskType;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface LiaisonService {

    List<AgentMeta> agentMetas();

    List<String> getAgentAddress(List<AgentMeta> agentMetas);

    void hookTaskReportLock(String taskNo);

    InterProcessMutex acquireTaskStartLock() throws Exception;

    InterProcessMutex acquireTaskStartLock(long time, TimeUnit unit) throws Exception;

    InterProcessMutex acquireTaskReportLock(String taskNo) throws Exception;
    void setBarrier(String taskNo);

    void openBarrier(String taskNo);

    List<String> queryPressureAgent();

    List<String> queryPressureAgent(TaskType taskType, Integer id);

    void savePressureAgent(TaskType taskType, Integer id, List<String> agents);

    void deletePressureAgent(TaskType taskType, Integer id);

    Statistics queryPressureStatistics(String taskNo);

    void savePressureStatistics(Statistics statistics);

    void deletedPressureStatistics(String taskNo);

    void cancelTaskReportLock(String taskNo);

    void ensureRemoteDomain(String address);

    String getData(String zkPath);

    void updateData(String zkPath, String data);

    Boolean top300StressSwitch(Boolean topSwitch);

    Boolean getTop300StressSwitch();
}
