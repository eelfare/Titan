package com.yunji.titanrtx.common.message;


public enum ErrorMsg {

    SUCCESS(1, "成功"),

    ERROR(0, "失败"),


    ACCOUNT_EMPTY(1020, "账号密码密码不能为空"),
    USER_EXIST(1030, "用户不存在"),
    USERNAME_OR_PASSWORD_ERROR(1040, "用户名或密码错误"),
    PHONE_REPETITION(1050, "手机号码已存在"),


    PARAMS_BLANK(2000, "参数不能为空"),
    PARAMS_INPUT_ERROR(2001, "输入参数有误"),

    NO_PATH(9997, "当前操作未授权"),
    NO_PERMISSION(9998, "无权限操作"),
    NO_LOGIN(9999, "当前用户未登录"),


    SCENE_NOT_FOUND(2001, "当前场景不存在"),
    STRESS_DOING(2002, "当前场景正在压测中"),
    STRESS_MACHINE_LACK(2003, "可用Agent压测机器不足");

    private int code;

    private String msg;

    ErrorMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
