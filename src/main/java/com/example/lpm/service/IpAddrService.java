// package com.example.lpm.service;
//
// import java.util.concurrent.Future;
//
// import com.baomidou.mybatisplus.extension.service.IService;
// import com.example.lpm.domain.dto.LuminatiIPDTO;
// import com.example.lpm.domain.entity.IpAddrDO;
// import com.example.lpm.domain.query.FindIpQuery;
// import com.example.lpm.domain.query.IpAddrQuery;
// import com.example.lpm.v3.domain.query.PageQuery;
// import com.example.lpm.domain.request.LuminatiCollectIpRequest;
// import com.example.lpm.domain.vo.LuminatiProgressVO;
// import com.example.lpm.v3.domain.vo.PageVO;
//
// @Deprecated
// public interface IpAddrService extends IService<IpAddrDO> {
// PageVO<IpAddrDO> listPage(IpAddrQuery ipAddrQuery, PageQuery pageQuery);
//
// void updateRemark(Long id, String remark);
//
// void getLuminatiIpAddrByAsync(String country, String state, String city);
//
// Future<Boolean> manualHeartbeatAsync(Long id);
//
// void heartbeatAsync(Long id);
//
// Boolean heartbeat(Long id);
//
// Integer startProxyById(Integer id);
//
// IpAddrDO luaGetIp(FindIpQuery findIpQuery);
//
// void luaReportIp(Long id, Integer useState, String remark);
//
// Boolean luaCheckIp(Long id);
//
// LuminatiIPDTO checkXLuminatiIpAndIp(String xLuminatiIp, String ip);
//
// void collect(LuminatiCollectIpRequest luminatiCollectIpRequest);
//
// LuminatiProgressVO collectProgress();
//
// void endCollect();
//
// void pauseCollect();
//
// }
