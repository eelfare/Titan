package com.yunji.titanrtx.manager.dao.entity.data;

import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 任务批次
 *
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:27
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BatchEntity extends BaseEntity {
    private String name;                    // 批次名称
    private Integer target;                     // 批次执行目标的任务ID
    private int dataTotal;                  // 目标数据总数
    private int status;                     // 数据构造状态（0：未开始,1：构造中，2：构造完成，3：构造失败，4：已导出，5：导出中，6：导出失败）
    private String tasks;                   // 任务清单
    private Date doTime;                    // 任务执行时间
}
