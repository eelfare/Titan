package com.yunji.titanrtx.common.enums;

public enum ParamMode {

    RANDOM("random"),
    ORDER("order");

    private String memo;

    ParamMode(String memo){
        this.memo=memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

}
