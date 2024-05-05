package com.yunji.titanrtx.common.enums;

import java.io.Serializable;

/**
 * 参数输出结果的数据类型
 */
public enum OutputType implements Serializable {

    STRING("string"),
    ARRAY("array");

    private String memo;

    OutputType(String memo){
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }


}
