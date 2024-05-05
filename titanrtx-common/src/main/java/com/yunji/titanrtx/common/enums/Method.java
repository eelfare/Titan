package com.yunji.titanrtx.common.enums;

public enum Method {

    GET ("get"),
    POST("post");

    private String memo;

    Method(String memo){
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }


}
