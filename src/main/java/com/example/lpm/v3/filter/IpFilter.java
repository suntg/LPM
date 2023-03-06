package com.example.lpm.v3.filter;


import com.example.lpm.util.IpUtil;
import com.example.lpm.v3.domain.entity.OperationLogDO;
import com.example.lpm.v3.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class IpFilter implements Filter {

    private List<String> urlPatterns = Arrays.asList("/luminati/getProxyPort", "/lua/createProxyPort",
            "/proxyIp/createProxyPort", "/rola/startSocksPort");

    @Resource
    private OperationLogService operationLogService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // 如果请求的URL包含在指定的URL列表中，执行过滤操作，否则继续执行链中的下一个过滤器
        if (urlPatterns.stream().anyMatch(requestURI::contains)) {
            // 执行您需要的过滤操作
            OperationLogDO operationLogDO = new OperationLogDO();
            operationLogDO.setRequestUri(requestURI);
            operationLogDO.setIp(IpUtil.getIpAddr(httpRequest));
            operationLogService.save(operationLogDO);
        } else {
            chain.doFilter(request, response);
        }
    }
}
