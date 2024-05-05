package com.yunji.titanrtx.manager.service.data;

import com.yunji.titanrtx.manager.dao.entity.data.BatchEntity;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:53
 * @Version 1.0
 */
public interface TaskBatchService {
    List<BatchEntity> selectAll();

    BatchEntity findById(int id);

    int deleteById(int id);

    Integer insert(BatchEntity taskBatch);

    int update(BatchEntity taskBatch);

    int updateDataTotal(BatchEntity batchEntity);

    List<BatchEntity> searchBatchs(String key);

    Boolean usedTargetTask(int target);
}
