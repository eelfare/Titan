package com.yunji.titanrtx.manager.dao.bos.auto;

import com.yunji.titanrtx.common.enums.AutoTestGrading;
import lombok.Data;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 29/4/2020 10:34 上午
 * @Version 1.0
 */
@Data
public class AutoDeployBo {
    private Integer id;
    /**
     * 自动化类型 0：普通自动化压测任务 1：TOP300自动化压测任务  2：批次自动化执行任务
     */
    private AutoType type;
    /**
     * 具体业务
     */
    private Integer businessId;
    private String name;
    private String time;
    private AutoTestGrading grading;
    /**
     * 压测持续时间
     */
    private int continuousTime;

    public enum AutoType{
        COMMON_STRESS, TOP_STRESS, BATCH
    }
}
