package com.example.lpm.v3.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.v3.common.ReturnCode;
import com.example.lpm.v3.domain.entity.PortWhitelistDO;
import com.example.lpm.v3.domain.entity.RolaIpDO;
import com.example.lpm.v3.domain.entity.RolaProxyPortDO;
import com.example.lpm.v3.domain.query.FindSocksPortQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.query.RolaQuery;
import com.example.lpm.v3.domain.request.RolaIpActiveRequest;
import com.example.lpm.v3.domain.request.RolaIpLockRequest;
import com.example.lpm.v3.domain.request.RolaIpRequest;
import com.example.lpm.v3.domain.request.RolaStartSocksPortRequest;
import com.example.lpm.v3.domain.vo.PageVO;
import com.example.lpm.v3.domain.vo.RolaProgressVO;
import com.example.lpm.v3.service.PortWhitelistService;
import com.example.lpm.v3.service.RolaIpService;
import com.example.lpm.v3.service.RolaProxyPortService;
import com.example.lpm.v3.util.ExecuteCommandUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Tag(name = "Rola")
@Slf4j
@RestController
@RequestMapping("/rola")
@RequiredArgsConstructor
public class RolaController {

    private final RolaIpService rolaIpService;

    private final RolaProxyPortService rolaProxyPortService;

    @Operation(summary = "收集")
    @PostMapping("/collect")
    public void collect(RolaIpRequest rolaIpRequest) {
        rolaIpService.collect(rolaIpRequest);
    }

    @Operation(summary = "分页查询Rola IP")
    @GetMapping("/listRolaIpsPage")
    public PageVO<RolaIpDO> listRolaIpsPage(RolaQuery rolaQuery, PageQuery pageQuery) {
        return rolaIpService.listRolaIpsPage(rolaQuery, pageQuery);
    }

    @Operation(summary = "分页查询FileName")
    @GetMapping("/listFilesPage")
    public PageVO<RolaIpDO> listFilesPage(RolaQuery rolaQuery, PageQuery pageQuery) {
        return rolaIpService.listFilesPage(rolaQuery, pageQuery);
    }

    @Operation(summary = "分页查询FileName")
    @PostMapping("/deleteFile")
    public void deleteFile(Long id) {
        rolaIpService.deleteFile(id);
    }

    @Operation(summary = "批量删除")
    @PostMapping("/deleteFiles")
    public void deleteFile(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            rolaIpService.deleteFile(id);
        }
    }


    @Operation(summary = "启动代理端口")
    @PostMapping("/startProxyPort")
    public void startProxyPort(RolaIpRequest rolaIpRequest) throws Exception {
        rolaProxyPortService.startProxyPort(rolaIpRequest);
    }

    @Operation(summary = "更换代理IP")
    @PostMapping("/changeProxyIp")
    public void changeProxyIp(RolaIpRequest rolaIpRequest) throws Exception {
        rolaProxyPortService.changeProxyIp(rolaIpRequest);
    }

    @Operation(summary = "收集进度")
    @GetMapping("/collectProgress")
    public RolaProgressVO collectProgress() {
        return rolaIpService.collectProgress();
    }

    @Operation(summary = "分页查询代理端口")
    @GetMapping("/listProxyPortsPage")
    public PageVO<RolaProxyPortDO> listProxyPortsPage(RolaQuery rolaQuery, PageQuery pageQuery) {
        return rolaProxyPortService.listPortsPage(rolaQuery, pageQuery);
    }

    @Operation(summary = "通过ID删除代理端口")
    @PostMapping("/deleteProxyPortById")
    public void deleteProxyPortById(@RequestParam Long id) {
        rolaProxyPortService.deleteProxyPort(id);
    }

    @Operation(summary = "通过IP删除代理端口")
    @PostMapping("/deleteProxyPortByIP")
    public void deleteProxyPortByIp(@RequestParam String ip) {
        rolaProxyPortService.deleteProxyPortByIp(ip);
    }

    @Operation(summary = "查询可用Rola IP")
    @PostMapping("/findSocksPort")
    public RolaIpDO findSocksPort(@RequestBody FindSocksPortQuery findSocksPortQuery) {
        return rolaIpService.findSocksPort(findSocksPortQuery);
    }


    private final PortWhitelistService portWhitelistService;

    @Operation(summary = "启动端口")
    @PostMapping("/startSocksPort")
    public boolean startSocksPort(@RequestBody RolaStartSocksPortRequest startSocksPortRequest) {

        long count = portWhitelistService.count(new QueryWrapper<PortWhitelistDO>().lambda().eq(PortWhitelistDO::getPort, startSocksPortRequest.getSocksPort()));
        if (count > 0) {
            throw new BizException(ReturnCode.RC500.getCode(), "端口为常用端口或项目使用中端口，更换重试");
        }
        if (ExecuteCommandUtil.portOccupancy(startSocksPortRequest.getSocksPort())) {
            throw new BizException(ReturnCode.RC500.getCode(), "端口为常用端口或项目使用中端口，更换重试");
        }

        return rolaProxyPortService.startSocksPort(startSocksPortRequest);
    }

    @Operation(summary = "删除端口")
    @PostMapping("/deleteSocksPort")
    public void deleteProxyPortByPort(@RequestParam Integer socksPort) {
        rolaProxyPortService.deleteProxyPortByPort(socksPort);
    }

    @Operation(summary = "批量删除端口")
    @PostMapping("/deleteBatchSocksPorts")
    public void deleteBatchSocksPorts(@RequestBody List<Integer> socksPorts) {
        rolaProxyPortService.deleteBatchSocksPorts(socksPorts);
    }

    @Operation(summary = "删除所有端口")
    @PostMapping("/deleteAllSocksPort")
    public void deleteAllSocksPort() {
        rolaProxyPortService.deleteAllProxyPort();
    }

    @Operation(summary = "提交IP锁定")
    @PostMapping("/submitIPLock")
    public void submitIpLock(@RequestBody RolaIpLockRequest rolaIpLockRequest) {
        rolaIpService.submitIpLock(rolaIpLockRequest);
    }

    @Operation(summary = "检查IP是否被锁定")
    @PostMapping("/checkIPLock")
    public RolaIpDO checkIpLock(@RequestBody RolaIpLockRequest rolaIpLockRequest) {
        return rolaIpService.checkIpLock(rolaIpLockRequest);
    }

    @Operation(summary = "指定测活")
    @PostMapping("/checkIPActive")
    public RolaIpDO checkIpActive(@RequestBody RolaIpActiveRequest rolaIpLockRequest) throws Exception {
        return rolaIpService.checkIpActive(rolaIpLockRequest);
    }

    @Operation(summary = "批量测活")
    @PostMapping("/checkIPsActive")
    public List<RolaIpDO> checkIpsActive(@RequestBody List<RolaIpActiveRequest> rolaIpLockRequests) throws Exception {
        ArrayList<RolaIpDO> rolaIpDOs = new ArrayList<>();
        for (RolaIpActiveRequest rolaIpLockRequest : rolaIpLockRequests) {
            RolaIpDO rolaIpDO = null;
            try {
                rolaIpDO = rolaIpService.checkIpActive(rolaIpLockRequest);
            } catch (Exception e) {

            }
            if (rolaIpDO != null) {
                rolaIpDOs.add(rolaIpDO);
            }
        }
        return rolaIpDOs;
    }

    @Operation(summary = "批量测活")
    @PostMapping("/checkIpsActiveAsync")
    public List<RolaIpDO> checkIpsActiveAsync(@RequestBody List<RolaIpActiveRequest> rolaIpLockRequests) throws Exception {
        List<RolaIpDO> rolaIpDOs = new ArrayList<>();
        if (CollUtil.isNotEmpty(rolaIpLockRequests)) {
            for (RolaIpActiveRequest rolaIpLockRequest : rolaIpLockRequests) {
                Future<RolaIpDO> future = rolaIpService.checkIpActiveAsync(rolaIpLockRequest);
                rolaIpDOs.add(future.get());
            }
        }
        return rolaIpDOs;
    }


    @Operation(summary = "查询fileFlag所有数据")
    @GetMapping("/listByFileFlag")
    public List<RolaIpDO> listByFileFlag(@RequestParam() String fileFlag,
                                         @RequestParam(required = false) String fileType) {
        return rolaIpService.listByFileFlag(fileFlag, fileType);
    }

    @Operation(summary = "保存IP")
    @PostMapping("/saveIP")
    public void saveIp(@RequestBody RolaIpDO rolaIpDO) {
        rolaIpService.save(rolaIpDO);
    }

    @Operation(summary = "结束收集")
    @GetMapping("/endCollect")
    public void endCollect() {
        rolaIpService.endCollect();
    }

    @Operation(summary = "暂停收集")
    @GetMapping("/pauseCollect")
    public void pauseCollect() {
        rolaIpService.pauseCollect();
    }

    @Operation(summary = "收集")
    @PostMapping("/phoneCollect")
    public void phoneCollect(RolaIpRequest rolaIpRequest) {
        rolaIpService.phoneCollect(rolaIpRequest);
    }

    @GetMapping("/country-state-city")
    public void downloadTextFile(HttpServletResponse response) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("country-state-city.txt");
        InputStream inputStream = classPathResource.getInputStream();
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
    }

    @Operation(summary = "收集")
    @PostMapping("/collectV2")
    public void collectV2(RolaIpRequest rolaIpRequest) {
        rolaIpService.collectV2(rolaIpRequest);
    }

}
