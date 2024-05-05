package com.yunji.titanrtx.common.enums;

import java.io.Serializable;

/**
 * 参数输出结果的数据来源
 */
public enum OutputSource implements Serializable {

    PARAM("param"),
    RESPONSE("response"),
    IGNORE("ignore");

    private String memo;

    OutputSource(String memo){
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }


}
