package com.yunji.titanrtx.manager.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.common.message.ErrorMsg;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.bos.UserSessionBo;
import com.yunji.titanrtx.manager.dao.entity.system.OpsLogEntity;
import com.yunji.titanrtx.manager.dao.entity.system.PathEntity;
import com.yunji.titanrtx.manager.dao.entity.system.UserEntity;
import com.yunji.titanrtx.manager.service.system.OpsLogService;
import com.yunji.titanrtx.manager.service.system.PathService;
import com.yunji.titanrtx.manager.service.system.UserService;
import com.yunji.titanrtx.manager.web.support.Constant;
import com.yunji.titanrtx.manager.web.support.u.RequestU;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 权限拦截接口
 * 所有涉及到*.do操作的  都需要验证权限
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final String INTERCEPTOR_SUFFIX = ".do";
    private static final String DOCX_SUFFIX = ".docx";

    @Resource
    private UserService userService;

    @Resource
    private OpsLogService opsLogService;

    @Resource
    private PathService pathService;


    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String uri = httpServletRequest.getRequestURI();
        //.docx
        if (uri.lastIndexOf(INTERCEPTOR_SUFFIX) != -1) {

            UserSessionBo bo = RequestU.getUserSessionBo(httpServletRequest.getSession());

            if (bo == null) {
                return uri.endsWith(DOCX_SUFFIX);
            }

            // 如果是root 则直接通行
            uri = uri.split("/")[1];
            PathEntity path = pathService.findByUriPath(uri);

            if (path == null) {
                RequestU.toJson(RespMsg.respErr(ErrorMsg.NO_PATH), httpServletResponse);
                return false;
            }

            UserEntity user = userService.findUserByPhone(bo.getPhone());
            if (!bo.isRoot()) {
                if (!user.getPathIds().contains(String.valueOf(path.getId()))) {
                    RequestU.toJson(RespMsg.respErr(ErrorMsg.NO_PERMISSION), httpServletResponse);
                    return false;
                }
            }
            opsLogService.insert(new OpsLogEntity(user.getId(), bo.getUserName(), path.getName(), JSON.toJSONString(httpServletRequest.getParameterMap())));
            return true;
        }
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
