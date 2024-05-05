package com.yunji.titanrtx.manager.web.advice;

import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.CollectionU;
import com.yunji.titanrtx.manager.web.support.u.RequestU;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全局异常处理
 */
@ControllerAdvice
public class GlobalExceptionAdvice {

    @Value("${online:false}")
    private boolean online;


    /** 请求没有相应的处理 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public void forbidden(NoHandlerFoundException e, HttpServletResponse response) throws IOException {
        RequestU.toJson(RespMsg.respErr("无对应的请求"), response);
    }

    /** 请求方法不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public void notSupported(HttpRequestMethodNotSupportedException e,
                             HttpServletResponse response) throws IOException {

        String msg = null;
        if (!online) {
            msg = String.format(" 当前方式(%s), 支持方式(%s)", e.getMethod(), CollectionU.toStr(e.getSupportedMethods()));
        }
        RequestU.toJson(RespMsg.respErr("不支持此种请求方式"), response);
    }


    /** 未知的所有其他异常 */
    @ExceptionHandler(Exception.class)
    public void exception(Exception e, HttpServletResponse response) throws IOException {
        e.printStackTrace();
        RequestU.toJson(RespMsg.respErr(online ? "请求时出现错误, 我们将会尽快处理." : e.getMessage()), response);
    }

    /** 无效参数异常 */
    @ExceptionHandler(IllegalArgumentException.class)
    public void illegalArgumentException(Throwable e, HttpServletResponse response) throws IOException {
        RequestU.toJson(RespMsg.respErr(online ? "请求时出现错误, 我们将会尽快处理." : e.getMessage()), response);
    }

}