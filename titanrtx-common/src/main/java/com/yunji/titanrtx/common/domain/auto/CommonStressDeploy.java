package com.yunji.titanrtx.common.domain.auto;

import lombok.Data;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 7/4/2020 11:07 上午
 * @Version 1.0
 */
@Data
public class CommonStressDeploy extends AbstractDeploy {
    /**
     * 场景ID
     */
    Integer sceneId;
    /**
     * 持续压测时间
     */
    int continuousTime;


    public boolean isCommonStress() {
        return true;
    }
}
