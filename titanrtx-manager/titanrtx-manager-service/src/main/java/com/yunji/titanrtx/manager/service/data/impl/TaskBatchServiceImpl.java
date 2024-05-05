package com.yunji.titanrtx.manager.service.data.impl;

import com.yunji.titanrtx.manager.dao.entity.data.BatchEntity;
import com.yunji.titanrtx.manager.dao.mapper.data.TaskBatchMapper;
import com.yunji.titanrtx.manager.service.data.TaskBatchService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:53
 * @Version 1.0
 */
@Service
public class TaskBatchServiceImpl implements TaskBatchService {

    @Resource
    TaskBatchMapper taskBatchMapper;

    @Override
    public List<BatchEntity> selectAll() {
        return taskBatchMapper.selectAll();
    }

    @Override
    public BatchEntity findById(int id) {
        return taskBatchMapper.findById(id);
    }

    @Override
    public int deleteById(int id) {
        return taskBatchMapper.deleteById(id);
    }

    @Override
    public Integer insert(BatchEntity taskBatch) {
        return taskBatchMapper.insert(taskBatch);
    }

    @Override
    public int update(BatchEntity taskBatch) {
        return taskBatchMapper.update(taskBatch);
    }
    @Override
    public int updateDataTotal(BatchEntity batchEntity) {
        return taskBatchMapper.updateDataTotal(batchEntity);
    }

    @Override
    public List<BatchEntity> searchBatchs(String key) {
        return taskBatchMapper.searchBatchs(key);
    }

    @Override
    public Boolean usedTargetTask(int target) {
        return taskBatchMapper.usedTargetTask(target) == 0 ? false : true;
    }
}
