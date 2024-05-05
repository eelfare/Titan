package com.yunji.titanrtx.manager.dao.mapper.data;

import com.yunji.titanrtx.manager.dao.entity.data.TaskParamSourceEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 15:33
 * @Version 1.0
 */
public interface TaskParamSourceMapper {

    List<TaskParamSourceEntity> findByBatchId(int batchId);

    TaskParamSourceEntity findByBatchIdAndTaskId(@Param("batchId") int batchId, @Param("taskId") int taskId);

    int deleteById(int id);

    int deleteByBatchId(int batchId);

    int deleteByBatchIdAndTaskId(@Param("batchId") int batchId, @Param("taskId") int taskId);

    Integer insert(TaskParamSourceEntity taskBatch);

    int update(TaskParamSourceEntity taskBatch);
}
