package com.yunji.titanrtx.manager.dao.bos.data;

import com.yunji.titanrtx.manager.dao.entity.data.BatchEntity;
import com.yunji.titanrtx.manager.dao.entity.data.TaskEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 11:54
 * @Version 1.0
 */
@Data
public class BatchBo {
    private BatchEntity batchEntity;
    private TaskEntity targetTask;
    private List<TaskEntity> taskEntities;
}
