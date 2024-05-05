package com.yunji.titanrtx.manager.web.sso.entity;

import org.omg.PortableInterceptor.SUCCESSFUL;

import java.io.Serializable;

/**
 * Created by liudonghua on 17-11-10.
 */
public class SsoResponse<T> implements Serializable {

    public static final int CODE_SUCCESS=0;

    public static final int CODE_FAIL=-1;

    /**
     * 错误返回code,0成功 －1用户登陆失效
     */
    private int code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    public SsoResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public SsoResponse() {
        this(CODE_SUCCESS,"",null);
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
