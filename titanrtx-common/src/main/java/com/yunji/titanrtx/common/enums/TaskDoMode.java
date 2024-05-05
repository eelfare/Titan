package com.yunji.titanrtx.common.enums;

import java.io.Serializable;

/**
 * 任务执行方式
 */
public enum TaskDoMode implements Serializable {

    HTTP("HTTP"),
    DUBBO("DUBBO"),
    SOCKET("SOCKET"),
    FILE("FILE");

    private String memo;

    TaskDoMode(String memo) {
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }


    @Override
    public String toString() {
        return "TaskDoMode{" + "memo='" + memo + '\'' + '}';
    }
}
