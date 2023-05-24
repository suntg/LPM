package com.example.lpm.v1.filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.lpm.v1.domain.entity.OperationLogDO;
import com.example.lpm.v1.service.OperationLogService;
import com.example.lpm.v1.util.IpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// @Component
// 定义filterName 和过滤的url
// @WebFilter(filterName = "myFilter", urlPatterns = {"/luminati/getProxyPort", "/lua/createProxyPort",
// "/rola/startSocksPort"})
public class IpFilter implements Filter {

    /**
     * "/luminati/getProxyPort", "/lua/createProxyPort",
     */
    private final List<String> urlPatterns =
        Arrays.asList("/luminati/getProxyPort", "/lua/createProxyPort", "/rola/startSocksPort");

    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String requestURI = httpRequest.getRequestURI();

        // 如果请求的URL包含在指定的URL列表中，执行过滤操作，否则继续执行链中的下一个过滤器
        if (urlPatterns.stream().anyMatch(requestURI::contains)) {
            // 执行您需要的过滤操作
            OperationLogDO operationLogDO = new OperationLogDO();
            operationLogDO.setRequestUri("建立端口");
            operationLogDO.setIp(IpUtil.getIpAddr(httpRequest));

            try {
                String result = HttpUtil.get("https://ip.useragentinfo.com/json?ip=" + operationLogDO.getIp());
                JSONObject jsonObject = JSON.parseObject(result);
                operationLogDO.setCountry(jsonObject.getString("country"));
                operationLogDO.setRegion(jsonObject.getString("province"));
                operationLogDO.setCity(jsonObject.getString("city"));
            } catch (Exception e) {
                log.error("ip.useragentinfo.com 查询{}异常:{}", operationLogDO.getIp(),
                    ExceptionUtil.stacktraceToString(e));
            }

            operationLogDO.setCreateTime(LocalDateTime.now());
            operationLogService.save(operationLogDO);
        }
        chain.doFilter(request, response);
    }
}
