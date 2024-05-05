package com.yunji.titanrtx.common.message;


import lombok.ToString;

@ToString
public class RespMsg extends Message {

    private static final long serialVersionUID = 1L;

    private String msg;

    private int code;

    private Object data;

    public RespMsg() {
    }

    public RespMsg(String msg, int code) {
        super();
        this.msg = msg;
        this.code = code;
    }

    public RespMsg(ErrorMsg msg) {
        this(msg.getMsg(), msg.getCode());
    }

    public RespMsg(ErrorMsg msg, Object data) {
        this(msg.getMsg(), msg.getCode(), data);
    }

    public RespMsg(String msg, int code, Object data) {
        this(msg, code);
        this.data = data;
    }

    public static RespMsg respErr(ErrorMsg msg) {
        return new RespMsg(msg);
    }

    public static RespMsg respErr() {
        return new RespMsg(ErrorMsg.ERROR);
    }

    public static RespMsg respErr(String msg) {
        return new RespMsg(msg, ErrorMsg.ERROR.getCode());
    }

    public static RespMsg respErr(ErrorMsg msg, Object data) {
        return new RespMsg(msg, data);
    }

    public static RespMsg respSuc() {
        return respErr(ErrorMsg.SUCCESS);
    }

    public static RespMsg respSuc(String msg) {
        return new RespMsg(msg, ErrorMsg.SUCCESS.getCode());
    }

    public static RespMsg respSuc(Object data) {
        return respErr(ErrorMsg.SUCCESS, data);
    }

    public boolean isSuccess() {
        return code == 1;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }


    public static RespMsg respCom(int i) {
        if (i == 0) return RespMsg.respErr();
        return RespMsg.respSuc();
    }


}
