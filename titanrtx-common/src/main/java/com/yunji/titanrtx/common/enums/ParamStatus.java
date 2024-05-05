package com.yunji.titanrtx.common.enums;

public enum ParamStatus {
    /**
     * 参数乱序
     */
    OUT_OF_ORDER(0),
    /**
     * 参数顺序
     */
    ORDER(1);

    private int id;

    public int getId() {
        return id;
    }

    ParamStatus(int id) {
        this.id = id;
    }
}
