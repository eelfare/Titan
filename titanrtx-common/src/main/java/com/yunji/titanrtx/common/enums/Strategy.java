package com.yunji.titanrtx.common.enums;

public enum  Strategy {

    PEAK("peak"),
    GENTLY("gently"),
    FIXATION("fixation");

    private String memo;

    Strategy(String memo){
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

}
