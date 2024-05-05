package com.yunji.titanrtx.manager.web.support.u;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.bos.UserSessionBo;
import com.yunji.titanrtx.manager.web.support.Constant;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class RequestU {


    public static <T> void toJson(RespMsg result, HttpServletResponse response) throws IOException {
        String json = JSON.toJSONString(result);
        response.setContentType("application/json;charset=UTF-8;");
        response.getWriter().write(json);
        log.debug("return json: {}", json);
    }

    public static void removeSession(HttpSession session) {
        if (session == null) {
            return;
        }
        session.setAttribute(Constant.USER_LOGIN_SESSION, null);
    }


    public static UserSessionBo getUserSessionBo(HttpSession session) {
        return (UserSessionBo) session.getAttribute(Constant.USER_LOGIN_SESSION);
    }
}
