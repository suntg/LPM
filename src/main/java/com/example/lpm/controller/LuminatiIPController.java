package com.example.lpm.controller;

import com.alibaba.fastjson2.JSONArray;
import com.example.lpm.domain.entity.LuminatiIPDO;
import com.example.lpm.domain.request.DeleteProxyPortRequest;
import com.example.lpm.domain.request.LuminatiIPRequest;
import com.example.lpm.domain.request.LuminatiProxyRequest;
import com.example.lpm.service.LuminatiIPService;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.v3.common.ReturnCode;
import com.example.lpm.v3.util.PortUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Tag(name = "Luminati")
@Slf4j
@RestController
@RequestMapping("/luminati")
public class LuminatiIPController {

    @Resource
    private LuminatiIPService luminatiIPService;

    @Operation(summary = "通过proxy sps命令启动代理")
    @PostMapping("/getIPAndStartProxy")
    public LuminatiIPDO getIPAndStartProxy(@RequestBody LuminatiIPRequest luminatiIPRequest) {
        return luminatiIPService.getIPAndStartProxy(luminatiIPRequest);
    }

    @Operation(summary = "通过端口停止代理", description = "传入all，执行killall -9 proxy")
    @GetMapping("/stopProxyByPort")
    public void stopProxyByPort(@RequestParam String port) {
        luminatiIPService.stopProxyByPort(port);
    }

    @Operation(summary = "通过调用 /api/proxies 接口启动代理端口")
    @PostMapping("/getProxyPort")
    public void getProxyPort(@RequestBody LuminatiProxyRequest luminatiProxyRequest) {

        if (PortUtil.contains(luminatiProxyRequest.getProxyPort())) {
            throw new BizException(ReturnCode.RC500.getCode(), "端口为常用端口或项目使用中端口，更换重试");
        }

        luminatiIPService.getProxyPort(luminatiProxyRequest);
    }

    @Operation(summary = "通过调用 /api/proxies/{port} 接口删除代理端口")
    @PostMapping("/deleteProxyPort")
    public void deleteProxyPort(@RequestBody DeleteProxyPortRequest deleteProxyPortRequest) {
        luminatiIPService.deleteProxyPort(deleteProxyPortRequest);
    }

    @Operation(summary = "通过调用 /api/proxies_running 接口获取所有代理端口状态")
    @GetMapping("/stateProxyPort")
    public JSONArray stateProxyPort() {
        return luminatiIPService.stateProxyPort();
    }

}
