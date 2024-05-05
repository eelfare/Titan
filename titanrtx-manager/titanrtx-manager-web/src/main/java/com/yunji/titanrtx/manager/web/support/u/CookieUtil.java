package com.yunji.titanrtx.manager.web.support.u;


import com.sun.jndi.toolkit.url.UrlUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liuliang
 * @desc Cookie操作工具类
 * @date 2017年12月21日 上午11:51:13
 */
public class CookieUtil {
    private static final Logger logger = LoggerFactory.getLogger(CookieUtil.class);

    /**
     * @param request
     * @param name
     * @return
     * @desc 获取cookie中指定key的值
     * @author liuliang
     * @date 2017年12月21日 上午11:51:22
     */
    public static String getCookieValueByName(HttpServletRequest request, String name) {
        Cookie cookie = getCookieByName(request, name);
        if (cookie != null && StringUtils.isNotBlank(cookie.getValue())) {
            return cookie.getValue();
        } else {
            return null;
        }
    }


    /**
     * @param request
     * @param name
     * @return Cookie
     * @desc 获取cookie对象
     * @author liuliang
     */
    public static Cookie getCookieByName(HttpServletRequest request, String name) {
        Map<String, Cookie> cookieMap = ReadCookieMap(request);
        if (cookieMap.containsKey(name)) {
            Cookie cookie = (Cookie) cookieMap.get(name);
            return cookie;
        } else {
            return null;
        }
    }

    /**
     * @param request
     * @return Map<String, Cookie>
     * @desc 获取cookie中的数据，并返回map
     * @author liuliang
     */
    private static Map<String, Cookie> ReadCookieMap(HttpServletRequest request) {
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        Cookie[] cookies = request.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie);
            }
        }
        return cookieMap;
    }

    /**
     * 删除cookie
     *
     * @param response
     * @param key
     */
    public static void delCookie(HttpServletRequest request, HttpServletResponse response, String key) {
        Cookie cookie = getCookieByName(request, key);
        if(cookie == null) {
            return;
        }
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * sso退出登录
     *
     * @param response
     * @param key
     */
    public static void ssoLogout(HttpServletRequest request, HttpServletResponse response,String domain, String key) {
        Cookie cookie = getCookieByName(request, key);
        if (cookie == null) {
            return;
        }
        cookie.setMaxAge(0);
        cookie.setValue(null);
        String[] split = domain.split("\\.");
        if (split.length >= 3) {
            cookie.setDomain(split[split.length - 2] + "." + (split[split.length - 1].contains("/") ?
                    split[split.length - 1].substring(0, split[split.length - 1].indexOf("/")) :
                    split[split.length - 1]));
            cookie.setPath("/");
            response.addCookie(cookie);
        } else {
            logger.warn("web.domain【{}】有误",domain);
        }
    }


    /**
     * 添加cookie
     *
     * @param response
     * @param expiry
     */
    public static void addCookie(HttpServletResponse response, String key, String value, int expiry) {
        try {
            //关键点
            value = URLEncoder.encode(value, "UTF-8");
            Cookie cookie = new Cookie(key, value);
            cookie.setMaxAge(expiry);
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
