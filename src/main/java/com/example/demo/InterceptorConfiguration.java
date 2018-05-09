package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * 拦截器配置类
 */
@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer {

    @Autowired
    private LimitRaterInterceptor limitRaterInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(limitRaterInterceptor).addPathPatterns("/**");
    }
}
