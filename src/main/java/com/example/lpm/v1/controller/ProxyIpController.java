// package com.example.lpm.v3.controller;
//
// import com.example.lpm.v3.common.BizException;
// import com.example.lpm.v3.common.ReturnCode;
// import com.example.lpm.v3.util.PortUtil;
// import org.springframework.web.bind.annotation.*;
//
// import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// import com.example.lpm.v3.constant.ProxyIpType;
// import com.example.lpm.v3.domain.entity.ProxyIpDO;
// import com.example.lpm.v3.domain.query.PageQuery;
// import com.example.lpm.v3.domain.query.ProxyIpQuery;
// import com.example.lpm.v3.domain.request.CheckIpSurvivalRequest;
// import com.example.lpm.v3.domain.request.CollectionTaskRequest;
// import com.example.lpm.v3.domain.request.StartProxyPortRequest;
// import com.example.lpm.v3.domain.vo.CollectionProgressVO;
// import com.example.lpm.v3.service.ProxyIpService;
// import com.example.lpm.v3.strategy.ProxyStrategy;
// import com.example.lpm.v3.strategy.ProxyStrategyFactory;
//
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @Tag(name = "luminati rola IP")
// @Slf4j
// @RequestMapping("/proxyIp")
// @RestController
// @RequiredArgsConstructor
// public class ProxyIpController {
//
// private final ProxyIpService proxyIpService;
//
// private final ProxyStrategyFactory proxyStrategyFactory;
//
// @Operation(summary = "分页查询 IP 【合并】")
// @GetMapping("/page")
// public Page<ProxyIpDO> listProxyIpsByPage(PageQuery pageQuery, ProxyIpQuery proxyIpQuery) {
// return proxyIpService.listProxyIpsByPage(pageQuery, proxyIpQuery);
// }
//
// @Operation(summary = "添加收录任务 【合并】")
// @PostMapping("/addCollectionTask")
// public void addCollectionTask(@RequestBody CollectionTaskRequest collectionTaskRequest) {
// ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(collectionTaskRequest.getProxyIpType());
// proxyStrategy.addCollectionTask(collectionTaskRequest);
// }
//
// @Operation(summary = "查询收录进度 【合并】")
// @GetMapping("/getCollectionProgress")
// public CollectionProgressVO getCollectionProgress(ProxyIpType typeName) {
// return proxyIpService.getCollectionProgress(typeName);
// }
//
// @Operation(summary = "启动代理端口 【合并】")
// @PostMapping("/startProxyPort")
// public void startProxyPort(@RequestBody StartProxyPortRequest startProxyPortRequest) {
// if (PortUtil.contains(startProxyPortRequest.getProxyPort())) {
// throw new BizException(ReturnCode.RC500.getCode(), "端口为常用端口或项目使用中端口，更换重试");
// }
// ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(startProxyPortRequest.getProxyIpType());
// proxyStrategy.startProxyPort(startProxyPortRequest);
// }
//
// @Operation(summary = "检测存活 【合并】")
// @PostMapping("/checkIpSurvival")
// public String checkIpSurvival(@RequestBody CheckIpSurvivalRequest checkIpSurvivalRequest) {
// ProxyStrategy proxyStrategy = proxyStrategyFactory.findStrategy(checkIpSurvivalRequest.getProxyIpType());
// proxyStrategy.checkIpSurvival(checkIpSurvivalRequest);
// return checkIpSurvivalRequest.getIp();
// }
//
//
//
// }
