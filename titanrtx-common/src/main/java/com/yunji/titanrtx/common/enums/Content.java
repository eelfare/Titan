package com.yunji.titanrtx.common.enums;

public enum Content {

    XWWWFORMURLENCODED("application/x-www-form-urlencoded"),
    RAW("raw"),
    BINARY("binary"),
    Text("text/xml"),
    JSON("application/json"),
    JAVASCRIPT("application/javascript"),
    XML("application/xml"),
    HTML("text/html");

    private String memo;

    Content(String memo){
        this.memo=memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
