package com.yunji.titanrtx.manager.dao.entity.data;

import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务参数配置
 *
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:20
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskParamDeployEntity extends BaseEntity {
    private Integer taskId;                 // 任务ID
    private String name;                // 参数名
    private String extra;               // 参数说明信息
}
