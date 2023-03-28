package com.example.lpm.controller;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import com.example.lpm.v3.common.ReturnCode;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.constant.RedisKeyConstant;
import com.example.lpm.v3.domain.dto.FileDTO;
import com.example.lpm.domain.request.FileRequest;
import com.example.lpm.v3.service.FileService;

import cn.hutool.core.text.CharSequenceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Lua")
@Slf4j
@RestController
@RequestMapping("/lua")
public class LuaController {

    // @Resource
    // private IpAddrService ipAddrService;

    @Resource
    private FileService fileService;

    @Resource
    private RedissonClient redissonClient;

    // @Operation(summary = "lua测活IP")
    // @GetMapping("/checkIp")
    // public Boolean checkIp(@RequestParam Long id) {
    //     return ipAddrService.luaCheckIp(id);
    // }

    // @Operation(summary = "lua通过ip和xLuminatiIp测活IP")
    // @GetMapping("/checkXLuminatiIpAndIp")
    // public LuminatiIPDTO checkXLuminatiIpAndIp(@RequestParam String xLuminatiIp,
    //     @RequestParam(required = false) String ip) {
    //     return ipAddrService.checkXLuminatiIpAndIp(xLuminatiIp, ip);
    // }

    // @Operation(summary = "lua上报IP状态")
    // @PostMapping("/reportIp")
    // public void reportIp(@RequestParam Long id, @RequestParam Integer useState,
    //     @RequestParam(required = false) String remark) {
    //     ipAddrService.luaReportIp(id, useState, remark);
    // }

    // @Operation(summary = "lua获取IP")
    // @PostMapping("getIp")
    // public IpAddrDO getIp(@RequestBody FindIpQuery findIpQuery) {
    //     /*if (CharSequenceUtil.isAllBlank(findIpQuery.getIp(), findIpQuery.getZipCode(), findIpQuery.getState(),
    //         findIpQuery.getCity())) {
    //         throw new ServiceException(ReturnCode.RC500.getCode(), "传参不能同时为空");
    //     }*/
    //     return ipAddrService.luaGetIp(findIpQuery);
    //
    // }

    @Operation(summary = "lua获取file所有详情")
    @GetMapping("/getFile")
    public FileDTO getFile(@RequestParam(required = false) String fileName,
        @RequestParam(required = false) Long fileId) {
        return fileService.getFile(fileName, fileId);
    }

    @Operation(summary = "lua保存file")
    @PostMapping("/saveFile")
    public void getFile(@RequestBody FileRequest fileRequest) {
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

}
