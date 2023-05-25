package com.example.lpm.v1.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.example.lpm.constant.RedisKeyConstant;
import com.example.lpm.v1.common.BizException;
import com.example.lpm.v1.common.ReturnCode;
import com.example.lpm.v1.domain.dto.FileDTO;
import com.example.lpm.v1.domain.entity.LumIPDO;
import com.example.lpm.v1.domain.entity.OperationLogDO;
import com.example.lpm.v1.domain.query.FindSocksPortQuery;
import com.example.lpm.v1.domain.request.FileRequest;
import com.example.lpm.v1.domain.request.LumIPActiveRequest;
import com.example.lpm.v1.domain.request.LumIPCollectRequest;
import com.example.lpm.v1.domain.request.RolaIpLockRequest;
import com.example.lpm.v1.service.FileService;
import com.example.lpm.v1.service.LumIPService;
import com.example.lpm.v1.service.OperationLogService;
import com.example.lpm.v1.util.IpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.text.CharSequenceUtil;
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

    // private final ProxyIpService proxyIpService;

    // private final FileService fileService;

    // private final ProxyStrategyFactory proxyStrategyFactory;

    private final RedissonClient redissonClient;

    // private final RolaAddIpStrategy rolaAddIpStrategy;
    // private final LuminatiAddIpStrategy luminatiAddIpStrategy;

    // @Operation(summary = "lua获取IP 【合并】")
    // @PostMapping("getProxyIp")
    // public ProxyIpDO getProxyIp(@RequestBody LuaGetProxyIpQuery luaGetProxyIpQuery) {
    // ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(luaGetProxyIpQuery.getProxyIpType());
    // return proxyStrategy.getProxyIp(luaGetProxyIpQuery);
    // }
    //
    //
    // @Operation(summary = "lua通过File 获取Proxy 【合并】")
    // @PostMapping("/getProxyIpByFile")
    // public List<ProxyIpDO> getProxyIpByFile(@RequestBody ProxyFileQuery proxyFileQuery) {
    // return proxyIpService.getProxyIpByFile(proxyFileQuery);
    // }
    //
    // @Operation(summary = "lua更新IP 【合并】")
    // @PostMapping("/updateProxyIp")
    // public void updateProxyIp(@RequestBody UpdateProxyIpRequest updateProxyIpRequest) {
    // proxyIpService.updateProxyIp(updateProxyIpRequest);
    // }
    //
    // @Operation(summary = "lua保存IP 【合并】")
    // @PostMapping("/saveProxyIp")
    // public void saveProxyIp(@RequestBody ProxyIpDO proxyIpDO) {
    //
    // long result = this.proxyIpService.count(new QueryWrapper<ProxyIpDO>().lambda()
    // .eq(ProxyIpDO::getIp, proxyIpDO.getIp()).eq(ProxyIpDO::getTypeName, proxyIpDO.getTypeName()));
    // if (result < 1) {
    // this.proxyIpService.save(proxyIpDO);
    // } else {
    // throw new BizException("已经存在");
    // }
    //
    // }
    //
    // @Operation(summary = "lua测活IP 【合并】")
    // @PostMapping("/checkProxyIp")
    // public void checkProxyIp(@RequestBody CheckIpSurvivalRequest checkIpSurvivalRequest) {
    // ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(checkIpSurvivalRequest.getProxyIpType());
    // proxyStrategy.checkIpSurvival(checkIpSurvivalRequest);
    // }
    //
    // @Operation(summary = "lua收录IP 【合并】")
    // @PostMapping("/addProxyIp")
    // public void addProxyIp(CollectionTaskRequest collectionTaskRequest) {
    //
    // if (ProxyIpType.ROLA.getTypeName().equals(collectionTaskRequest.getProxyIpType().getTypeName())) {
    // rolaAddIpStrategy.addProxyIpTask(collectionTaskRequest);
    // } else if (ProxyIpType.LUMINATI.getTypeName().equals(collectionTaskRequest.getProxyIpType().getTypeName())) {
    // luminatiAddIpStrategy.addProxyIpTask(collectionTaskRequest);
    // } else {
    // throw new BizException(ReturnCode.RC500.getCode(), "失败");
    // }
    //
    // }
    //
    // @Operation(summary = "lua创建代理端口 【合并】")
    // @PostMapping("/createProxyPort")
    // public void createProxyPort(@RequestBody StartProxyPortRequest startProxyPortRequest) {
    //
    // if (PortUtil.contains(startProxyPortRequest.getProxyPort())) {
    // throw new BizException(ReturnCode.RC500.getCode(), "端口为常用端口或项目使用中端口，更换重试");
    // }
    //
    // ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(startProxyPortRequest.getProxyIpType());
    // proxyStrategy.startProxyPort(startProxyPortRequest);
    // }

    private final OperationLogService operationLogService;

    private final HttpServletRequest request;

    private final LumIPService lumIPService;
    @Resource
    private FileService fileService;

    @Resource
    private ObjectMapper objectMapper;

    @Operation(summary = "lua保存OperationLog")
    @PostMapping("/saveOperationLog")
    @Transactional(rollbackFor = Exception.class)
    public void createProxyPort(@RequestBody OperationLogDO operationLogDO) {
        operationLogDO.setIp(IpUtil.getIpAddr(request));
        operationLogDO.setSource(2);
        operationLogService.record(operationLogDO);
    }

    @Operation(summary = "lua获取file所有详情")
    @GetMapping("/getFile")
    public FileDTO getFile(@RequestParam(required = false) String fileName,
        @RequestParam(required = false) Long fileId) {
        return fileService.getFile(fileName, fileId);
    }

    @Operation(summary = "lua保存file")
    @PostMapping("/saveFile")
    public void saveFile(@RequestBody FileRequest fileRequest) {
        if (CharSequenceUtil.isBlank(fileRequest.getFileName())) {
            throw new BizException(ReturnCode.RC500.getCode(), "fileName不能为空");
        }
        RLock rLock = redissonClient.getLock(RedisKeyConstant.LOCK_FILE_NAME_KEY + fileRequest.getFileName());
        if (rLock.isLocked()) {
            throw new BizException(ReturnCode.RC500.getCode(), "获取锁失败");
        }
        rLock.lock(5, TimeUnit.SECONDS);
        try {
            fileService.saveFile(fileRequest);
        } finally {
            rLock.unlock();
        }
    }

    @Operation(summary = "提交IP锁定")
    @PostMapping("/submitIPLock")
    public void submitIpLock(@RequestBody RolaIpLockRequest rolaIpLockRequest) {
        lumIPService.submitIpLock(rolaIpLockRequest);
    }

    @Operation(summary = "查询fileFlag所有数据")
    @GetMapping("/listByFileFlag")
    public List<LumIPDO> listByFileFlag(@RequestParam() String fileFlag,
        @RequestParam(required = false) String fileType) {
        return lumIPService.listByFileFlag(fileFlag, fileType);
    }

    @Operation(summary = "查询可用Rola IP")
    @PostMapping("/findSocksPort")
    public LumIPDO findSocksPort(@RequestBody FindSocksPortQuery findSocksPortQuery) {
        return lumIPService.findSocksPort(findSocksPortQuery);
    }

    @Operation(summary = "指定测活")
    @PostMapping("/checkIPActive")
    public LumIPDO checkIpActive(@RequestBody LumIPActiveRequest lumIPActiveRequest) {
        return lumIPService.checkIpActive(lumIPActiveRequest);
    }

    @Operation(summary = "收集——LUM API")
    @PostMapping("/collectLumIP")
    public void collectLumIP(LumIPCollectRequest lumIPCollectRequest) {
        lumIPService.collect(lumIPCollectRequest);
    }

}
