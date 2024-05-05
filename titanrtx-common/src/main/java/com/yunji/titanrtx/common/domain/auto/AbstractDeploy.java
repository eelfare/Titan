package com.yunji.titanrtx.common.domain.auto;

import com.yunji.titanrtx.common.enums.AutoTestGrading;
import lombok.Data;

import java.io.Serializable;

/**
 * 常态的自动话配置信息
 * @author jingf
 */
@Data
public abstract class AbstractDeploy implements Serializable {
    /**
     * id
     */
    Integer id;
    /**
     * 名称
     */
    String name;
    /**
     * 执行时间
     */
    String time;
    /**
     * 调度粒度
     */
    AutoTestGrading grading;

    boolean history;
}
