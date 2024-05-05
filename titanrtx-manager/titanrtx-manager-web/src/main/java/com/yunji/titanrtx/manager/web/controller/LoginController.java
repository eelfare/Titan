package com.yunji.titanrtx.manager.web.controller;

import com.yunji.titanrtx.common.message.ErrorMsg;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.bos.UserSessionBo;
import com.yunji.titanrtx.manager.dao.entity.system.UserEntity;
import com.yunji.titanrtx.manager.service.system.UserService;
import com.yunji.titanrtx.manager.web.config.SystemConfig;
import com.yunji.titanrtx.manager.web.support.Constant;
import com.yunji.titanrtx.manager.web.support.u.CookieUtil;
import com.yunji.titanrtx.manager.web.support.u.RequestU;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;


@Slf4j
@RestController
@RequestMapping("/backup/login/")
public class LoginController {

    @Resource
    private UserService userService;

    @Resource
    private SystemConfig systemConfig;

    @RequestMapping("verify.query")
    public RespMsg verify(HttpSession session, HttpServletRequest request, @NonNull String phone, @NonNull String passWord) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            System.out.println("【" +name+ "】 ： 【" + request.getHeader(name)+"】");
        }
        log.info("用户登录,phone:{}.....................................................", phone);

        UserEntity userEntity = userService.findUserByPhone(phone);
        if (userEntity == null) return RespMsg.respErr(ErrorMsg.USER_EXIST);
        if (userEntity.getDeleted() == 1) return RespMsg.respErr("当前用户已被禁用，请联系相关人员解除禁用");
        if (!StringUtils.equalsIgnoreCase(userEntity.getPassWord(), passWord))
            return RespMsg.respErr(ErrorMsg.USERNAME_OR_PASSWORD_ERROR);

        UserSessionBo bo = new UserSessionBo();
        userService.increaseLogTimes(userEntity);
        bo.setUserName(userEntity.getUserName());
        bo.setPhone(userEntity.getPhone());
        session.setAttribute(Constant.USER_LOGIN_SESSION, bo);
        return RespMsg.respSuc();
    }


    @RequestMapping("layout.query")
    public RespMsg layout(HttpSession session) {
        RequestU.removeSession(session);
        return RespMsg.respSuc();
    }


}
