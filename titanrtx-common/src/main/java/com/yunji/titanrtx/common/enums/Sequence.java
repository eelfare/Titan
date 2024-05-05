package com.yunji.titanrtx.common.enums;

public enum  Sequence {

    IN("in"),
    BUNCH("bunch"),
    OUT("out");

    private String memo;

    Sequence(String memo){
        this.memo=memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

}
