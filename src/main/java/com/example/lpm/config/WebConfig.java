package com.example.lpm.config;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.lpm.interceptor.LoginInterceptor;

/**
 * 1、编写一个拦截器实现HandlerInterceptor接口 2、拦截器注册到容器中（实现WebMvcConfigurer的addInterceptors） 3、指定拦截规则【如果是拦截所有，静态资源也会被拦截】
 *
 * @EnableWebMvc:全面接管 1、静态资源？视图解析器？欢迎页.....全部失效
 */
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**") // 所有请求都被拦截包括静态资源
            .excludePathPatterns("/", "/login", "/css/**", "/fonts/**", "/images/**", "/js/**", "/aa/**"); // 放行的请求

    }
}
