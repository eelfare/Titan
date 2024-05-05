package com.yunji.titanrtx.manager.dao.bos.data;

import com.yunji.titanrtx.manager.dao.entity.data.TaskEntity;
import lombok.Data;

import java.util.List;

/**
 * 批次中的某个任务详细配置信息
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 15:43
 * @Version 1.0
 */
@Data
public class TaskSourceBo {
    private Integer id; // 任务的参数配置信息ID
    private TaskEntity taskEntity; // 当前任务基本信息
    private List<ParamSourceBo> paramSourceBos; //当前任务中的所有入参的配置信息
    private List<TaskEntity> batchOwnTask; // 当前批次的其他任务列表
}
