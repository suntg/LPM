package com.example.lpm.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import com.example.lpm.domain.request.LuminatiCollectIpRequest;
import com.example.lpm.domain.vo.*;
import org.springframework.web.bind.annotation.*;

import com.example.lpm.domain.entity.IpAddrDO;
import com.example.lpm.domain.query.IpAddrQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.service.IpAddrService;
import com.example.lpm.service.XxlJobService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "IpAddr")
@Slf4j
@RestController
@RequestMapping("/ipAddr")
public class IpAddrController {

    @Resource
    private IpAddrService ipAddrService;

    @Resource
    private XxlJobService xxlJobService;

    @Operation(summary = "分页查询")
    @GetMapping("listPage")
    public PageVO<IpAddrDO> listPage(IpAddrQuery ipAddrQuery, PageQuery pageQuery) {
        return ipAddrService.listPage(ipAddrQuery, pageQuery);
    }

    @Operation(summary = "通过Id更新备注")
    @Parameter(name = "id", required = true, description = "ID")
    @Parameter(name = "remark", required = true, description = "备注")
    @PostMapping("/updateRemark")
    public void updateRemark(@RequestParam Long id, @RequestParam String remark) {
        ipAddrService.updateRemark(id, remark);
    }

    @Operation(summary = "手动获取获取LuminatiIp")
    @Parameter(name = "number", required = true, description = "获取IP次数")
    @PostMapping("/getLuminatiIpAddr")
    public void getLuminatiIpAddr(@RequestParam Integer number, @RequestParam(required = false) String country,
        @RequestParam(required = false) String state, @RequestParam(required = false) String city) {
        for (int i = 0; i < number; i++) {
            ipAddrService.getLuminatiIpAddrByAsync(country, state, city);
        }
    }

    @Operation(summary = "获取所有任务")
    @GetMapping("/listJob")
    public List<JobInfoVO> listJob() {
        return xxlJobService.listJob();
    }

    @Operation(summary = "更新任务")
    @Parameters({@Parameter(name = "id", required = true, description = "任务ID"),
        @Parameter(name = "intervalMinute", required = true, description = "执行任务间隔时间，分钟"),
        @Parameter(name = "executorPararm", required = false, description = "执行参数")})
    @PostMapping("updateJob")
    public void updateJob(@RequestParam int id, @RequestParam int intervalMinute,
        @RequestParam(required = false) String executorPararm) {
        String schduleConf = CharSequenceUtil.format("0 0/{} * * * ?", intervalMinute);
        xxlJobService.updateById(id, schduleConf, executorPararm);
    }

    @Operation(summary = "启动任务")
    @Parameter(name = "id", required = true, description = "任务ID")
    @PostMapping("startJob")
    public void startJob(@RequestParam int id) {
        xxlJobService.startById(id);
    }

    @Operation(summary = "停止任务")
    @Parameter(name = "id", required = true, description = "任务ID")
    @PostMapping("stopJob")
    public void stopJob(@RequestParam int id) {
        xxlJobService.stopById(id);
    }

    @Operation(summary = "手动进行所有IP心跳")
    @PostMapping("manualHeartbeatAllIp")
    public void manualHeartbeatAllIp() {
        xxlJobService.triggerById(3);
    }

    @Operation(summary = "手动进行心跳")
    @GetMapping("manualHeartbeat")
    public List<HeartbeatVO> manualHeartbeat(@RequestParam(value = "idList") List<Long> idList)
        throws ExecutionException, InterruptedException {
        List<HeartbeatVO> heartbeatVOList = new ArrayList<>();
        if (CollUtil.isNotEmpty(idList)) {
            for (Long id : idList) {
                Future<Boolean> future = ipAddrService.manualHeartbeatAsync(id);
                HeartbeatVO heartbeatVO = new HeartbeatVO();
                heartbeatVO.setId(id);
                heartbeatVO.setResult(future.get());
                heartbeatVOList.add(heartbeatVO);
            }
        }
        return heartbeatVOList;
    }

    @Operation(summary = "开启代理端口")
    @Parameter(name = "id", required = true, description = "IP ID")
    @PostMapping("startProxy")
    public Integer startProxy(@RequestParam Integer id) {
        return ipAddrService.startProxyById(id);
    }

    @Operation(summary = "获取xxl-job执行日志pageList")
    @GetMapping("/getLogList")
    public String getLogList(Integer id) {
        return xxlJobService.logPageList(id);
    }

    @Operation(summary = "收集")
    @PostMapping("/collect")
    public void collect(LuminatiCollectIpRequest luminatiCollectIpRequest) {
        ipAddrService.collect(luminatiCollectIpRequest);
    }

    @Operation(summary = "收集进度")
    @GetMapping("/collectProgress")
    public LuminatiProgressVO collectProgress() {
         return ipAddrService.collectProgress();
    }

    @Operation(summary = "结束收集")
    @GetMapping("/endCollect")
    public void endCollect() {
        ipAddrService.endCollect();
    }

    @Operation(summary = "暂停收集")
    @GetMapping("/pauseCollect")
    public void pauseCollect() {
        ipAddrService.pauseCollect();
    }

}
