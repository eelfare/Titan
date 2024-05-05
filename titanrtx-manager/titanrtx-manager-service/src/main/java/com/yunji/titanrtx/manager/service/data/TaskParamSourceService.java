package com.yunji.titanrtx.manager.service.data;

import com.yunji.titanrtx.manager.dao.entity.data.TaskParamSourceEntity;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:53
 * @Version 1.0
 */
public interface TaskParamSourceService {
    List<TaskParamSourceEntity> findByBatchId(int batchId);

    TaskParamSourceEntity findByBatchIdAndTaskId(int batchId, int taskId);

    int deleteById(int id);

    int deleteByBatchId(int batchId);

    int deleteByBatchIdAndTaskId(int batchId, int taskId);

    Integer insert(TaskParamSourceEntity taskBatch);

    int update(TaskParamSourceEntity taskBatch);
}
