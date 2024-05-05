package com.yunji.titanrtx.common.domain.auto;

import lombok.Data;

/**
 * @author jingf
 */
@Data
public class TopStressDeploy extends CommonStressDeploy {
    /**
     * top压测第几波 top压测才使用
     */
    TopOrder topOrder;

    public enum TopOrder {
        DEFAULT,
        FIRST,
        SECOND,
        THIRD
    }

    public boolean isTopStress() {
        return true;
    }
}
