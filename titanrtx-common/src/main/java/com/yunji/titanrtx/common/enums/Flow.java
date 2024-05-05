package com.yunji.titanrtx.common.enums;

public enum  Flow {

    AVERAGE("average"),
    AUTO("auto");

    private String memo;

    Flow(String memo){
        this.memo=memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
