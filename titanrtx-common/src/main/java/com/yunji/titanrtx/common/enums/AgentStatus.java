package com.yunji.titanrtx.common.enums;

public enum AgentStatus {

    IDLE("idle"),
    RUNNING("running"),
    DISABLE("disable");

    private String memo;

    AgentStatus(String memo){
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
