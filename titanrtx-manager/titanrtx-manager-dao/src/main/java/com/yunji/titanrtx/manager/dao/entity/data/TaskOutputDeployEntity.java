package com.yunji.titanrtx.manager.dao.entity.data;

import com.yunji.titanrtx.common.enums.OutputSource;
import com.yunji.titanrtx.common.enums.OutputType;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务保存结果配置信息
 *
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:22
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskOutputDeployEntity extends BaseEntity {
    private Integer taskId;                 // 任务ID
    private String name;                // 列名称
    private String expr;                // 值获取方式
    private OutputSource source;        // 数据来源
    private OutputType type;            // 数据类型
}
