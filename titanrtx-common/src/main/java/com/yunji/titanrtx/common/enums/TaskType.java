package com.yunji.titanrtx.common.enums;

import java.io.Serializable;

public enum TaskType implements Serializable {

    HTTP("HTTP"),
    DUBBO("DUBBO"),
    OUTSIDE("OUTSIDE");

    private String memo;

    TaskType(String memo){
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }


}
