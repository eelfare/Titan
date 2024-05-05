package com.yunji.titanrtx.manager.dao.bos.data.template;

import lombok.Data;

import java.util.List;

/**
 * 任务执行配置文件模板类
 *
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 17:09
 * @Version 1.0
 */
@Data
public class TaskTemplateBo {
    private int order;                      // 任务执行顺序
    private String name;                    // 任务名称
    private List<Param> params;             // 任务参数描述列表
    private Executor executor;              // 任务执行体
    private Output output;                  // 输出规则
}
