package com.yunji.titanrtx.manager.web.config;

import com.yunji.titanrtx.manager.web.interceptor.LoginInterceptor;
import com.yunji.titanrtx.manager.web.interceptor.PermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {


    @Resource
    private LoginInterceptor loginInterceptor;

    @Resource
    private PermissionInterceptor permissionInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截规则：除了login，其他都拦截判断
        registry.addInterceptor(loginInterceptor);
        registry.addInterceptor(permissionInterceptor);
    }


}
