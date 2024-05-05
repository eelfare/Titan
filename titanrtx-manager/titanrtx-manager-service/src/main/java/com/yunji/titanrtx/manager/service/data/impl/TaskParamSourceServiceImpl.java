package com.yunji.titanrtx.manager.service.data.impl;

import com.yunji.titanrtx.manager.dao.entity.data.TaskParamSourceEntity;
import com.yunji.titanrtx.manager.dao.mapper.data.TaskParamSourceMapper;
import com.yunji.titanrtx.manager.service.data.TaskParamSourceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 16:32
 * @Version 1.0
 */
@Service
public class TaskParamSourceServiceImpl implements TaskParamSourceService {

    @Resource
    TaskParamSourceMapper taskParamSourceMapper;

    @Override
    public List<TaskParamSourceEntity> findByBatchId(int batchId) {
        return taskParamSourceMapper.findByBatchId(batchId);
    }

    @Override
    public int deleteByBatchId(int batchId) {
        return taskParamSourceMapper.deleteByBatchId(batchId);
    }

    @Override
    public int deleteByBatchIdAndTaskId(int batchId, int taskId) {
        return taskParamSourceMapper.deleteByBatchIdAndTaskId(batchId,taskId);
    }

    @Override
    public TaskParamSourceEntity findByBatchIdAndTaskId(int batchId, int taskId) {
        return taskParamSourceMapper.findByBatchIdAndTaskId(batchId, taskId);
    }

    @Override
    public int deleteById(int id) {
        return taskParamSourceMapper.deleteById(id);
    }

    @Override
    public Integer insert(TaskParamSourceEntity taskBatch) {
        return taskParamSourceMapper.insert(taskBatch);
    }

    @Override
    public int update(TaskParamSourceEntity taskBatch) {
        return taskParamSourceMapper.update(taskBatch);
    }
}
