package com.yunji.titanrtx.manager.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.UserSessionBo;
import com.yunji.titanrtx.manager.dao.entity.system.UserEntity;
import com.yunji.titanrtx.manager.service.common.SystemProperties;
import com.yunji.titanrtx.manager.service.common.eventbus.EventBusCenter;
import com.yunji.titanrtx.manager.service.support.AccessTokenHandler;
import com.yunji.titanrtx.manager.service.system.UserService;
import com.yunji.titanrtx.manager.web.sso.entity.SsoResponse;
import com.yunji.titanrtx.manager.web.sso.entity.SsoUser;
import com.yunji.titanrtx.manager.web.support.Constant;
import com.yunji.titanrtx.manager.web.support.u.CookieUtil;
import com.yunji.titanrtx.manager.web.support.u.RequestU;
import com.yunji.titanrtx.plugin.http.HttpSyncClientTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private SystemProperties systemProperties;
    @Resource
    private UserService userService;

    @Value("${login.url:https://sso.yunjiglobal.com/login.html}")
    private String loginUrl;
    @Value("${token.valid.url:https://sso.yunjiglobal.com/userInfo/token}")
    private String tokenValidUrl;
    @Value("${web.domain:https://titanx-tx.yunjiglobal.com/}")
    private String webDomain;
    @Value("${default.permission:1}")
    private String defaultPermission;

    private static final String LOGIN_URL = "/backup/login/verify.query";
    private static final String LOGOUT_URL = "/backup/login/layout.query";
    private static final String MAIN_URL = "/view/main.html";
    private static final String ERROR_URL_PREFIX = "/error";
    private static final String EXPOSE_URI = "/expose/api/";

    private static final String EXPORT_ALERT = "/export/render/";
    private static final String EXPORT_REPORT = "/export/report/";


    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) {
        String uri = httpServletRequest.getRequestURI();
        //log.info("current uri {}", uri);

        if (LOGIN_URL.equalsIgnoreCase(uri) || ERROR_URL_PREFIX.equalsIgnoreCase(uri)) {
            return true;
        }

        if (!CommonU.isIDC() && uri.startsWith(EXPOSE_URI)) {
            return true;
        }

        if (uri.startsWith(EXPORT_ALERT) || uri.startsWith(EXPORT_REPORT)) {
            if (AccessTokenHandler.canUrlAccess(systemProperties.webSiteDomain + uri)) {
                return true;
            }
        }

        if (LOGOUT_URL.equalsIgnoreCase(uri)) {
            CookieUtil.ssoLogout(httpServletRequest, httpServletResponse, webDomain, Constant.SSO_COOKIE_NAME);
            redirectToLogin(httpServletResponse, loginUrl + "?backUrl=" + webDomain + MAIN_URL);
            RequestU.removeSession(httpServletRequest.getSession());
            return false;
        }
        UserSessionBo bo = RequestU.getUserSessionBo(httpServletRequest.getSession());
        if (bo != null) {
            return true;
        }
        String jessionId = CookieUtil.getCookieValueByName(httpServletRequest, Constant.SSO_COOKIE_NAME);
        if (StringUtils.isEmpty(jessionId)) {
            redirectToLogin(httpServletResponse, loginUrl + "?backUrl=" + webDomain + MAIN_URL);
            return false;
        }

        // 验证jessonId
        String result = HttpSyncClientTool.doGet(tokenValidUrl + "/" + jessionId, null, "Accept=application/json", "UTF-8");
        SsoResponse<SsoUser> dataResponse = null;
        SsoUser ssoUser = null;
        try {
            dataResponse = JSON.parseObject(result, new TypeReference<SsoResponse<SsoUser>>() {
            });
            if (dataResponse == null || dataResponse.getCode() != 1) {
                redirectToLogin(httpServletResponse, loginUrl + "?backUrl=" + webDomain + MAIN_URL);
                return false;
            }
            ssoUser = dataResponse.getData();
        } catch (Exception ex) {
            log.error("sso 系统出现问题，进入原始登录");
            redirectToLogin(httpServletResponse, "/backup/login.html");
            return false;
        }
        // 判断用户是否存在
        UserEntity entity = new UserEntity();
        if (userService.findUserByPhone(ssoUser.getPhone()) == null) {
            entity.setUserName(ssoUser.getEmpId());
            entity.setNickName(ssoUser.getUserName());
            entity.setPhone(ssoUser.getPhone());

            entity.setPassWord("a8sdfasdgzbdjkas234ksdf_" + ThreadLocalRandom.current().nextInt(100));
            //默认拥有的权限
            entity.setPathIds(defaultPermission);
            userService.insert(entity);
        }


        //查询数据库中的数据.
        String phone = ssoUser.getPhone();
        UserEntity ue = userService.findUserByPhone(phone);
        bo = new UserSessionBo();
        //11 是用户管理
        if ((StringUtils.isNotBlank(phone) && phone.equals("root")) || (ue != null && ue.getPathIds().contains("11"))) {
            bo.setRoot(true);
        } else {
            bo.setRoot(Constant.ROOT.contains(ssoUser.getEmpId()));
        }

        bo.setPhone(ssoUser.getPhone());
        bo.setUserName(ssoUser.getUserName());
        httpServletRequest.getSession().setAttribute(Constant.USER_LOGIN_SESSION, bo);
        //发送用户登陆,修改用户登陆次数
        fireUserLoginEvent(bo);

        return true;
    }


    private void redirectToLogin(HttpServletResponse response, String redirectUrl) {
        response.setHeader("redirect_url", redirectUrl);
        //log.info("redirect url {}", redirectUrl);
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object
            o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse
            httpServletResponse, Object o, Exception e) throws Exception {

    }


    private void fireUserLoginEvent(UserSessionBo bo) {
        EventBusCenter.post(bo);
    }
}
