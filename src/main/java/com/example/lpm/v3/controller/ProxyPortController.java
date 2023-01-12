package com.example.lpm.v3.controller;

import org.springframework.web.bind.annotation.*;

import com.example.lpm.domain.vo.PageVO;
import com.example.lpm.v3.domain.entity.ProxyPortDO;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.query.ProxyPortQuery;
import com.example.lpm.v3.domain.request.DeleteProxyPortRequest;
import com.example.lpm.v3.service.ProxyPortService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Port")
@Slf4j
@RequestMapping("/proxyPort")
@RestController
@RequiredArgsConstructor
public class ProxyPortController {

    private final ProxyPortService proxyPortService;

    @Operation(summary = "分页查询 【合并】")
    @GetMapping("/listProxyPortsByPage")
    public PageVO<ProxyPortDO> listProxyPortsByPage(ProxyPortQuery proxyPortQuery, PageQuery pageQuery) {
        return proxyPortService.listProxyPortsByPage(proxyPortQuery, pageQuery);
    }

    @Operation(summary = "通过ID删除代理端口  【合并】")
    @PostMapping("/deleteProxyPortById")
    public void deleteProxyPortById(@RequestParam Long id) {
        proxyPortService.deleteProxyPort(id);
    }

    @Operation(summary = "通过IP删除代理端口  【合并】")
    @PostMapping("/deleteProxyPortByIP")
    public void deleteProxyPortByIp(DeleteProxyPortRequest deleteProxyPortRequest) {
        proxyPortService.deleteProxyPortByIp(deleteProxyPortRequest);
    }




    @Operation(summary = "删除所有端口")
    @PostMapping("/deleteAllProxyPort")
    public void deleteAllSocksPort() {
        proxyPortService.deleteAllProxyPort();
    }


    // TODO 更换



}
