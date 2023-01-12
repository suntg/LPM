package com.example.lpm.v3.controller;

import java.util.List;

import com.example.lpm.v3.addIp.strategy.LuminatiAddIpStrategy;
import com.example.lpm.v3.addIp.strategy.RolaAddIpStrategy;
import com.example.lpm.v3.common.ReturnCode;
import com.example.lpm.v3.constant.ProxyIpType;
import com.example.lpm.v3.domain.request.CollectionTaskRequest;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.v3.domain.entity.ProxyIpDO;
import com.example.lpm.v3.domain.query.LuaGetProxyIpQuery;
import com.example.lpm.v3.domain.query.ProxyFileQuery;
import com.example.lpm.v3.domain.request.CheckIpSurvivalRequest;
import com.example.lpm.v3.domain.request.StartProxyPortRequest;
import com.example.lpm.v3.domain.request.UpdateProxyIpRequest;
import com.example.lpm.v3.service.ProxyIpService;
import com.example.lpm.v3.strategy.ProxyStrategy;
import com.example.lpm.v3.strategy.ProxyStrategyFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Lua")
@Slf4j
@RestController("luaV3Controller")
@RequestMapping("/lua")
@RequiredArgsConstructor
public class LuaController {

    private final ProxyIpService proxyIpService;

    // private final FileService fileService;

    private final ProxyStrategyFactory proxyStrategyFactory;

    private final RedissonClient redissonClient;

    private final RolaAddIpStrategy rolaAddIpStrategy;
    private final LuminatiAddIpStrategy luminatiAddIpStrategy;

    @Operation(summary = "lua获取IP 【合并】")
    @PostMapping("getProxyIp")
    public ProxyIpDO getProxyIp(@RequestBody LuaGetProxyIpQuery luaGetProxyIpQuery) {
        ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(luaGetProxyIpQuery.getProxyIpType());
        return proxyStrategy.getProxyIp(luaGetProxyIpQuery);
    }

    /*@Operation(summary = "保存IP")
    @PostMapping("/saveIP")
    public void saveIp(@RequestBody ProxyIpDO proxyIpDO) {
        proxyIpService.save(proxyIpDO);
    }
    
    @Operation(summary = "lua获取file所有详情")
    @GetMapping("/getFile")
    public FileVO getFile(@RequestParam(required = false) String fileName,
        @RequestParam(required = false) Long fileId) {
        return fileService.getFile(fileName, fileId);
    }
    
    @Operation(summary = "lua保存file")
    @PostMapping("/saveFile")
    public void saveFile(@RequestBody @Validated FileRequest fileRequest) {
        RLock rLock = redissonClient.getLock(RedisLockKeyConstants.LUA_SAVE_FILE_KEY + fileRequest.getFileName());
        if (rLock.isLocked()) {
            throw new BizException(ReturnCode.RC500.getCode(), "获取锁失败");
        }
        rLock.lock(5, TimeUnit.SECONDS);
        try {
            fileService.saveFile(fileRequest);
        } finally {
            rLock.unlock();
        }
    }*/

    @Operation(summary = "lua通过File 获取Proxy 【合并】")
    @PostMapping("/getProxyIpByFile")
    public List<ProxyIpDO> getProxyIpByFile(@RequestBody ProxyFileQuery proxyFileQuery) {
        return proxyIpService.getProxyIpByFile(proxyFileQuery);
    }

    @Operation(summary = "lua更新IP 【合并】")
    @PostMapping("/updateProxyIp")
    public void updateProxyIp(@RequestBody UpdateProxyIpRequest updateProxyIpRequest) {
        proxyIpService.updateProxyIp(updateProxyIpRequest);
    }

    @Operation(summary = "lua保存IP 【合并】")
    @PostMapping("/saveProxyIp")
    public void saveProxyIp(@RequestBody ProxyIpDO proxyIpDO) {

        long result = this.proxyIpService.count(new QueryWrapper<ProxyIpDO>().lambda()
            .eq(ProxyIpDO::getIp, proxyIpDO.getIp()).eq(ProxyIpDO::getTypeName, proxyIpDO.getTypeName()));
        if (result < 1) {
            this.proxyIpService.save(proxyIpDO);
        } else {
            throw new BizException("已经存在");
        }

    }

    @Operation(summary = "lua测活IP 【合并】")
    @PostMapping("/checkProxyIp")
    public void checkProxyIp(@RequestBody CheckIpSurvivalRequest checkIpSurvivalRequest) {
        ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(checkIpSurvivalRequest.getProxyIpType());
        proxyStrategy.checkIpSurvival(checkIpSurvivalRequest);
    }

    @Operation(summary = "lua收录IP 【合并】")
    @PostMapping("/addProxyIp")
    public void addProxyIp(CollectionTaskRequest collectionTaskRequest) {

        if (ProxyIpType.ROLA.getTypeName().equals(collectionTaskRequest.getProxyIpType().getTypeName())) {
            rolaAddIpStrategy.addProxyIpTask(collectionTaskRequest);
        } else if (ProxyIpType.LUMINATI.getTypeName().equals(collectionTaskRequest.getProxyIpType().getTypeName())) {
            luminatiAddIpStrategy.addProxyIpTask(collectionTaskRequest);
        } else {
            throw new BizException(ReturnCode.RC500.getCode(),"失败");
        }

    }

    @Operation(summary = "lua创建代理端口 【合并】")
    @PostMapping("/createProxyPort")
    public void createProxyPort(@RequestBody StartProxyPortRequest startProxyPortRequest) {
        ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(startProxyPortRequest.getProxyIpType());
        proxyStrategy.startProxyPort(startProxyPortRequest);
    }

}
