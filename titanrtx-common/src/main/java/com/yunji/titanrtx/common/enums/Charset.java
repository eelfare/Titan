package com.yunji.titanrtx.common.enums;

public enum Charset {

    UTF8("UTF-8"),
    ISO88591("ISO-8859-1"),
    USASCII("US-ASCII"),
    UTF16("UTF-16"),
    UTF16LE("UTF-16LE"),
    UTF16BE("UTF-16BE");

    private String memo;

    Charset(String memo){
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }


}
