package com.example.lpm.v3.config;

import com.example.lpm.v3.filter.IpFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<IpFilter> myFilterRegistration() {
        FilterRegistrationBean<IpFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new IpFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
