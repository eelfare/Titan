package com.yunji.titanrtx.manager.dao.mapper.data;

import com.yunji.titanrtx.manager.dao.entity.data.BatchEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:38
 * @Version 1.0
 */
@Mapper
public interface TaskBatchMapper {
    List<BatchEntity> selectAll();

    BatchEntity findById(int id);

    int deleteById(int id);

    Integer insert(BatchEntity taskBatch);

    int update(BatchEntity taskBatch);

    int updateDataTotal(BatchEntity batchEntity);

    List<BatchEntity> searchBatchs(String key);

    int usedTargetTask(int target);
}
