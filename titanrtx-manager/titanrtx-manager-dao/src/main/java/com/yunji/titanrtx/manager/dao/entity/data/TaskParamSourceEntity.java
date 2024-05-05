package com.yunji.titanrtx.manager.dao.entity.data;

import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 15:07
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskParamSourceEntity extends BaseEntity {
    private Integer batchId;
    private Integer taskId;
    private String paramsSource; // 参数名_使用方式_数据来源类型_source source=> 固定值、任务ID_关联数据库列_过滤字段（无则用NULL代替）_对应参数名
}
