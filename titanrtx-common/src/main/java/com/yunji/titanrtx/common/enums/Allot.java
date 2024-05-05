package com.yunji.titanrtx.common.enums;

public enum Allot {

    WEIGHT("weight"),
    QPS("qps");

    private String memo;

    Allot(String memo) {
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

}
