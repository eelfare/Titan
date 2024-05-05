package com.yunji.titanrtx.common.enums;

public enum Protocol {

    HTTP("http"),
    HTTPS("https");

    private String memo;

    Protocol(String memo){
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
