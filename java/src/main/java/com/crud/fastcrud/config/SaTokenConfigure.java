package com.crud.fastcrud.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("========== 正在注册 Sa-Token 全局拦截器！=========="); // 新加这一句
        registry.addInterceptor(new SaInterceptor(h -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/login")
                .excludePathPatterns("/api/auth/register");

        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
