package com.example.lpm.v3.controller;

import com.example.lpm.util.IpUtil;
import com.example.lpm.v3.domain.entity.OperationLogDO;
import com.example.lpm.v3.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Tag(name = "Lua")
@Slf4j
@RestController("luaV3Controller")
@RequestMapping("/lua")
@RequiredArgsConstructor
public class LuaController {

    // private final ProxyIpService proxyIpService;

    // private final FileService fileService;

    // private final ProxyStrategyFactory proxyStrategyFactory;

    private final RedissonClient redissonClient;

    // private final RolaAddIpStrategy rolaAddIpStrategy;
    // private final LuminatiAddIpStrategy luminatiAddIpStrategy;

    // @Operation(summary = "lua获取IP 【合并】")
    // @PostMapping("getProxyIp")
    // public ProxyIpDO getProxyIp(@RequestBody LuaGetProxyIpQuery luaGetProxyIpQuery) {
    //     ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(luaGetProxyIpQuery.getProxyIpType());
    //     return proxyStrategy.getProxyIp(luaGetProxyIpQuery);
    // }
    //
    //
    // @Operation(summary = "lua通过File 获取Proxy 【合并】")
    // @PostMapping("/getProxyIpByFile")
    // public List<ProxyIpDO> getProxyIpByFile(@RequestBody ProxyFileQuery proxyFileQuery) {
    //     return proxyIpService.getProxyIpByFile(proxyFileQuery);
    // }
    //
    // @Operation(summary = "lua更新IP 【合并】")
    // @PostMapping("/updateProxyIp")
    // public void updateProxyIp(@RequestBody UpdateProxyIpRequest updateProxyIpRequest) {
    //     proxyIpService.updateProxyIp(updateProxyIpRequest);
    // }
    //
    // @Operation(summary = "lua保存IP 【合并】")
    // @PostMapping("/saveProxyIp")
    // public void saveProxyIp(@RequestBody ProxyIpDO proxyIpDO) {
    //
    //     long result = this.proxyIpService.count(new QueryWrapper<ProxyIpDO>().lambda()
    //             .eq(ProxyIpDO::getIp, proxyIpDO.getIp()).eq(ProxyIpDO::getTypeName, proxyIpDO.getTypeName()));
    //     if (result < 1) {
    //         this.proxyIpService.save(proxyIpDO);
    //     } else {
    //         throw new BizException("已经存在");
    //     }
    //
    // }
    //
    // @Operation(summary = "lua测活IP 【合并】")
    // @PostMapping("/checkProxyIp")
    // public void checkProxyIp(@RequestBody CheckIpSurvivalRequest checkIpSurvivalRequest) {
    //     ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(checkIpSurvivalRequest.getProxyIpType());
    //     proxyStrategy.checkIpSurvival(checkIpSurvivalRequest);
    // }
    //
    // @Operation(summary = "lua收录IP 【合并】")
    // @PostMapping("/addProxyIp")
    // public void addProxyIp(CollectionTaskRequest collectionTaskRequest) {
    //
    //     if (ProxyIpType.ROLA.getTypeName().equals(collectionTaskRequest.getProxyIpType().getTypeName())) {
    //         rolaAddIpStrategy.addProxyIpTask(collectionTaskRequest);
    //     } else if (ProxyIpType.LUMINATI.getTypeName().equals(collectionTaskRequest.getProxyIpType().getTypeName())) {
    //         luminatiAddIpStrategy.addProxyIpTask(collectionTaskRequest);
    //     } else {
    //         throw new BizException(ReturnCode.RC500.getCode(), "失败");
    //     }
    //
    // }
    //
    // @Operation(summary = "lua创建代理端口 【合并】")
    // @PostMapping("/createProxyPort")
    // public void createProxyPort(@RequestBody StartProxyPortRequest startProxyPortRequest) {
    //
    //     if (PortUtil.contains(startProxyPortRequest.getProxyPort())) {
    //         throw new BizException(ReturnCode.RC500.getCode(), "端口为常用端口或项目使用中端口，更换重试");
    //     }
    //
    //     ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(startProxyPortRequest.getProxyIpType());
    //     proxyStrategy.startProxyPort(startProxyPortRequest);
    // }

    private final OperationLogService operationLogService;

    private final HttpServletRequest request;

    @Operation(summary = "lua保存OperationLog")
    @PostMapping("/saveOperationLog")
    public void createProxyPort(@RequestBody OperationLogDO operationLogDO) {
        operationLogDO.setIp(IpUtil.getIpAddr(request));
        operationLogService.save(operationLogDO);
    }
}
