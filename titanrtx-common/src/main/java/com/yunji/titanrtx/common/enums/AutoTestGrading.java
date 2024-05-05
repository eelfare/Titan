package com.yunji.titanrtx.common.enums;

/**
 * 自动压测粒度
 */
public enum AutoTestGrading {
    PRECISE("precise"),
    EVERYDAY("everyday"),
    EVERYHOUR("everyhour"),
    EVERYMINUTE("everyminute");

    private String memo;

    AutoTestGrading(String memo) {
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

}
