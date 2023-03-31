package com.example.lpm.v3.filter;


import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.http.HttpUtil;
import com.example.lpm.util.IpUtil;
import com.example.lpm.v3.domain.dto.Ip123InfoDTO;
import com.example.lpm.v3.domain.entity.OperationLogDO;
import com.example.lpm.v3.service.OperationLogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
// 定义filterName 和过滤的url
@WebFilter(filterName = "myFilter", urlPatterns = {"/luminati/getProxyPort", "/lua/createProxyPort", "/rola/startSocksPort"})
public class IpFilter implements Filter {

    /**
     * "/luminati/getProxyPort", "/lua/createProxyPort",
     */
    private List<String> urlPatterns = Arrays.asList("/luminati/getProxyPort", "/lua/createProxyPort", "/rola/startSocksPort");

    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // 如果请求的URL包含在指定的URL列表中，执行过滤操作，否则继续执行链中的下一个过滤器
        if (urlPatterns.stream().anyMatch(requestURI::contains)) {
            // 执行您需要的过滤操作
            OperationLogDO operationLogDO = new OperationLogDO();
            operationLogDO.setRequestUri("建立端口");
            operationLogDO.setIp(IpUtil.getIpAddr(httpRequest));

            try {
                String ip123InfoResult = HttpUtil.get("http://ip123.in/search_ip?ip=" + operationLogDO.getIp());
                JsonNode jsonNode = objectMapper.readTree(ip123InfoResult);
                Ip123InfoDTO ip123InfoDTO = objectMapper.readValue(jsonNode.get("data").toString(), Ip123InfoDTO.class);
                operationLogDO.setCountry(ip123InfoDTO.getCountry());
                operationLogDO.setCity(ip123InfoDTO.getCity());
                operationLogDO.setRegion(ip123InfoDTO.getRegion());
            } catch (Exception e) {
                log.error("ip123 查询异常:{}", ExceptionUtil.stacktraceToString(e));
            }


            operationLogDO.setCreateTime(LocalDateTime.now());
            operationLogService.save(operationLogDO);
        }
        chain.doFilter(request, response);
    }
}
