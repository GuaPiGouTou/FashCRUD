package com.crud.fastcrud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 获取 uploads 文件夹绝对路径
        String path = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

        // 2. 映射 /files/** -> 本地磁盘
        // 这里的 "file:" 前缀在 Windows/Linux 均有效
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + path);

        System.out.println(">>> 静态资源映射路径: " + path);
    }
}