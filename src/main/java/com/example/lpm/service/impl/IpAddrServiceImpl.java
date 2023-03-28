// /*
// package com.example.lpm.service.impl;
//
// import static com.example.lpm.util.ExecuteCommandUtil.killProxyByPort;
//
// import java.net.InetSocketAddress;
// import java.net.Proxy;
// import java.time.LocalDateTime;
// import java.util.Comparator;
// import java.util.List;
// import java.util.concurrent.Future;
// import java.util.concurrent.TimeUnit;
// import java.util.stream.Collectors;
//
// import javax.annotation.Resource;
//
// import org.redisson.api.*;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.scheduling.annotation.AsyncResult;
// import org.springframework.stereotype.Service;
//
// import com.alibaba.fastjson2.JSON;
// import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
// import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
// import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// import com.ejlchina.okhttps.HTTP;
// import com.example.lpm.v3.common.ReturnCode;
// import com.example.lpm.v3.common.BizException;
// import com.example.lpm.config.LuminatiProperties;
// import com.example.lpm.constant.RedisKeyConstant;
// import com.example.lpm.domain.dto.LuminatiIPDTO;
// import com.example.lpm.domain.entity.IpAddrDO;
// import com.example.lpm.domain.query.FindIpQuery;
// import com.example.lpm.domain.query.IpAddrQuery;
// import com.example.lpm.v3.domain.query.PageQuery;
// import com.example.lpm.v3.domain.query.ZipCodeQuery;
// import com.example.lpm.domain.request.LuminatiCollectIpRequest;
// import com.example.lpm.domain.request.LuminatiIPRequest;
// import com.example.lpm.v3.domain.request.RolaIpRequest;
// import com.example.lpm.domain.vo.LuminatiProgressVO;
// import com.example.lpm.v3.domain.vo.PageVO;
// import com.example.lpm.mapper.IpAddrMapper;
// import com.example.lpm.service.IpAddrService;
// import com.example.lpm.util.ExecuteCommandUtil;
// import com.github.pagehelper.Page;
// import com.github.pagehelper.PageHelper;
//
// import cn.hutool.core.collection.CollUtil;
// import cn.hutool.core.date.DateUtil;
// import cn.hutool.core.exceptions.ExceptionUtil;
// import cn.hutool.core.text.CharSequenceUtil;
// import cn.hutool.core.util.ObjectUtil;
// import cn.hutool.core.util.RandomUtil;
// import cn.hutool.http.HttpRequest;
// import cn.hutool.http.HttpResponse;
// import cn.hutool.http.HttpStatus;
// import lombok.extern.slf4j.Slf4j;
//
// @Deprecated
// @Slf4j
// @Service
// public class IpAddrServiceImpl extends ServiceImpl<IpAddrMapper, IpAddrDO> implements IpAddrService {
//
//     @Resource
//     private HTTP http;
//
//     @Resource
//     private IpAddrMapper ipAddrMapper;
//
//     @Resource
//     private LuminatiProperties luminatiProperties;
//     @Resource
//     private RedissonClient redissonClient;
//     @Resource
//     private RedisTemplate<String, Object> redisTemplate;
//
//     @Override
//     public PageVO<IpAddrDO> listPage(IpAddrQuery ipAddrQuery, PageQuery pageQuery) {
//         Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
//         List<IpAddrDO> ipAddrDOList = ipAddrMapper.selectList(new QueryWrapper<IpAddrDO>().lambda()
//             .eq(ObjectUtil.isNotNull(ipAddrQuery.getState()), IpAddrDO::getState, ipAddrQuery.getState())
//             .likeRight(CharSequenceUtil.isNotBlank(ipAddrQuery.getIp()), IpAddrDO::getIp, ipAddrQuery.getIp())
//             .eq(CharSequenceUtil.isNotBlank(ipAddrQuery.getCountry()), IpAddrDO::getCountry, ipAddrQuery.getCountry())
//             .eq(CharSequenceUtil.isNotBlank(ipAddrQuery.getCity()), IpAddrDO::getCity, ipAddrQuery.getCity())
//             .eq(CharSequenceUtil.isNotBlank(ipAddrQuery.getRegion()), IpAddrDO::getRegion, ipAddrQuery.getRegion())
//             .eq(CharSequenceUtil.isNotBlank(ipAddrQuery.getXLuminatiIp()), IpAddrDO::getXLuminatiIp,
//                 ipAddrQuery.getXLuminatiIp())
//             .likeRight(CharSequenceUtil.isNotBlank(ipAddrQuery.getPostalCode()), IpAddrDO::getPostalCode,
//                 ipAddrQuery.getPostalCode())
//             .eq(IpAddrDO::getState, 1)
//             .ge(ObjectUtil.isNotNull(ipAddrQuery.getStartCreateTime()), IpAddrDO::getCreateTime,
//                 ipAddrQuery.getStartCreateTime())
//             .le(ObjectUtil.isNotNull(ipAddrQuery.getEndCreateTime()), IpAddrDO::getCreateTime,
//                 ipAddrQuery.getEndCreateTime())
//             .ge(ObjectUtil.isNotNull(ipAddrQuery.getStartHeartbeatTime()), IpAddrDO::getCreateTime,
//                 ipAddrQuery.getStartHeartbeatTime())
//             .le(ObjectUtil.isNotNull(ipAddrQuery.getEndHeartbeatTime()), IpAddrDO::getCreateTime,
//                 ipAddrQuery.getEndHeartbeatTime())
//             .like(CharSequenceUtil.isNotBlank(ipAddrQuery.getRemark()), IpAddrDO::getRemark, ipAddrQuery.getRemark())
//             .orderByDesc(IpAddrDO::getLastHeartbeatTime).orderByDesc(IpAddrDO::getId));
//         if (CollUtil.isNotEmpty(ipAddrDOList)) {
//             for (IpAddrDO ipAddrDO : ipAddrDOList) {
//                 if (CharSequenceUtil.isNotBlank(ipAddrDO.getCity())) {
//                     String city = "";
//                     for (String s : CharSequenceUtil.split(ipAddrDO.getCity(), " ")) {
//                         city = city + CharSequenceUtil.upperFirst(s) + " ";
//                     }
//                     ipAddrDO.setCity(CharSequenceUtil.trim(city));
//                 }
//                 if (CharSequenceUtil.isNotBlank(ipAddrDO.getRegion())) {
//                     ipAddrDO.setRegion(ipAddrDO.getRegion().toUpperCase());
//                 }
//                 if (CharSequenceUtil.isNotBlank(ipAddrDO.getCountry())) {
//                     ipAddrDO.setCountry(ipAddrDO.getCountry().toUpperCase());
//                 }
//             }
//         }
//
//         return new PageVO<>(page.getTotal(), ipAddrDOList);
//     }
//
//     @Override
//     public void updateRemark(Long id, String remark) {
//         IpAddrDO ipAddrDO = new IpAddrDO();
//         ipAddrDO.setId(id);
//         ipAddrDO.setRemark(remark);
//         ipAddrMapper.updateById(ipAddrDO);
//     }
//
//     @Override
//     @Async("getLuminatiIpTaskExecutor")
//     public void getLuminatiIpAddrByAsync(String country, String state, String city) {
//         String url = "http://lumtest.com/myip.json";
//         try {
//             HttpResponse response;
//             if (CharSequenceUtil.isAllNotBlank(country, state, city)) {
//                 String proxyUsername = CharSequenceUtil.format(
//                     "lum-customer-c_99c3c376-zone-zone_city_ly-country-{}-state-{}-city-{}",
//                     CharSequenceUtil.cleanBlank(country).toLowerCase(),
//                     CharSequenceUtil.cleanBlank(state).toLowerCase(), CharSequenceUtil.cleanBlank(city).toLowerCase());
//                 response = HttpRequest.get(url)
//                     .setProxy(new Proxy(Proxy.Type.HTTP,
//                         new InetSocketAddress(luminatiProperties.getProxyHost(), luminatiProperties.getProxyPort())))
//                     .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
//             } else if (CharSequenceUtil.isBlank(country) && CharSequenceUtil.isAllNotBlank(state, city)) {
//                 String proxyUsername = CharSequenceUtil.format(
//                     "lum-customer-c_99c3c376-zone-zone_city_ly-country-us-state-{}-city-{}",
//                     CharSequenceUtil.cleanBlank(state).toLowerCase(), CharSequenceUtil.cleanBlank(city).toLowerCase());
//                 response = HttpRequest.get(url)
//                     .setProxy(new Proxy(Proxy.Type.HTTP,
//                         new InetSocketAddress(luminatiProperties.getProxyHost(), luminatiProperties.getProxyPort())))
//                     .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
//             } else if (CharSequenceUtil.isNotBlank(country) && CharSequenceUtil.isNotBlank(state)
//                 && CharSequenceUtil.isBlank(city)) {
//                 String proxyUsername =
//                     CharSequenceUtil.format("lum-customer-c_99c3c376-zone-zone_city_ly-country-{}-state-{}",
//                         CharSequenceUtil.cleanBlank(country).toLowerCase(),
//                         CharSequenceUtil.cleanBlank(state).toLowerCase());
//                 response = HttpRequest.get(url)
//                     .setProxy(new Proxy(Proxy.Type.HTTP,
//                         new InetSocketAddress(luminatiProperties.getProxyHost(), luminatiProperties.getProxyPort())))
//                     .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
//             } else if (CharSequenceUtil.isBlank(country) && CharSequenceUtil.isNotBlank(state)
//                 && CharSequenceUtil.isBlank(city)) {
//                 String proxyUsername =
//                     CharSequenceUtil.format("lum-customer-c_99c3c376-zone-zone_city_ly-country-us-state-{}",
//                         CharSequenceUtil.cleanBlank(state).toLowerCase());
//                 response = HttpRequest.get(url)
//                     .setProxy(new Proxy(Proxy.Type.HTTP,
//                         new InetSocketAddress(luminatiProperties.getProxyHost(), luminatiProperties.getProxyPort())))
//                     .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
//             } else {
//                 response = HttpRequest.get(url)
//                     .setProxy(new Proxy(Proxy.Type.HTTP,
//                         new InetSocketAddress(luminatiProperties.getProxyHost(), luminatiProperties.getProxyPort())))
//                     .basicProxyAuth(luminatiProperties.getProxyUsername(), luminatiProperties.getProxyPassword())
//                     .setReadTimeout(5000).execute();
//             }
//
//             log.info("getLuminatiIpAddrByAsync lumtest result : {}", response.toString());
//             if (HttpStatus.HTTP_OK == response.getStatus()) {
//                 String xLuminatiIP = response.header("x-luminati-ip");
//
//                 String luminatiResult = response.body();
//                 log.info("getLuminatiIpAddrByAsync lumtest response body : {}", luminatiResult);
//
//                 LuminatiIPDTO luminatiIPDTO = JSON.parseObject(luminatiResult, LuminatiIPDTO.class);
//                 if (CharSequenceUtil.isAllNotEmpty(luminatiIPDTO.getCountry(), luminatiIPDTO.getGeo().getRegion(),
//                     luminatiIPDTO.getGeo().getCity())) {
//                     log.info("luminatiIPDTO :{}", JSON.toJSONString(luminatiIPDTO));
//                     saveOrUpdate(luminatiIPDTO, xLuminatiIP);
//                 }
//             } else {
//                 log.info("lumtest not ok : {}", response.body());
//             }
//         } catch (Exception e) {
//             log.error("lumtest error : {}", ExceptionUtil.stacktraceToString(e));
//         }
//     }
//
//     @Override
//     @Async("manualHeartbeatTaskExecutor")
//     public Future<Boolean> manualHeartbeatAsync(Long id) {
//         Boolean result = heartbeat(id);
//         return new AsyncResult<>(result);
//     }
//
//     @Override
//     @Async("timedHeartbeatTaskExecutor")
//     public void heartbeatAsync(Long id) {
//         heartbeat(id);
//     }
//
//     @Override
//     public Boolean heartbeat(Long id) {
//         IpAddrDO ipAddrDO = ipAddrMapper.selectOne(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getId, id));
//
//         String proxyUsername = luminatiProperties.getProxyUsername() + "-ip-" + ipAddrDO.getXLuminatiIp();
//         log.info("heartbeat param id:{}, ip:{}, x-luminati-ip:{}", id, ipAddrDO.getIp(), ipAddrDO.getXLuminatiIp());
//         try {
//             HttpResponse response = HttpRequest.get("http://lumtest.com/myip.json?ip=" + ipAddrDO.getIp())
//                 .setProxy(new Proxy(Proxy.Type.HTTP,
//                     new InetSocketAddress(luminatiProperties.getProxyHost(), luminatiProperties.getProxyPort())))
//                 .basicProxyAuth(proxyUsername, luminatiProperties.getProxyPassword()).setReadTimeout(20000).execute();
//             log.info("Luminati HttpResponse:{}", response);
//
//             String luminatiResult = response.body();
//
//             LuminatiIPDTO luminatiIPDTO = JSON.parseObject(luminatiResult, LuminatiIPDTO.class);
//             if (CharSequenceUtil.equals(luminatiIPDTO.getIp(), ipAddrDO.getIp())) {
//                 // 如果相等，更新心跳时间
//                 ipAddrDO.setState(1);
//                 ipAddrDO.setLastHeartbeatTime(LocalDateTime.now());
//                 ipAddrDO.setHeartbeatQuantity(0);
//                 ipAddrMapper.updateById(ipAddrDO);
//                 return Boolean.TRUE;
//             } else {
//                 // 如果不相等
//                 heartbeatFail(ipAddrDO);
//                 return Boolean.FALSE;
//             }
//
//         } catch (Exception e) {
//             heartbeatFail(ipAddrDO);
//             log.error("heartbeatFail http异常: {}", ExceptionUtil.stacktraceToString(e));
//             return Boolean.FALSE;
//         }
//     }
//
//     @Override
//     public Integer startProxyById(Integer id) {
//         IpAddrDO ipAddrDO = ipAddrMapper.selectById(id);
//
//         if (ObjectUtil.isNotNull(ipAddrDO)) {
//             RQueue<Integer> queue = redissonClient.getQueue("Port_Queue");
//             Integer port = queue.poll();
//             log.info("取出的端口: {}", port);
//             LuminatiIPRequest luminatiIPRequest = new LuminatiIPRequest();
//             luminatiIPRequest.setSocksUsername(luminatiProperties.getSocksUsername());
//             luminatiIPRequest.setSocksPassword(luminatiProperties.getSocksPassword());
//             luminatiIPRequest.setCustomer(luminatiProperties.getCustomer());
//             luminatiIPRequest.setZone(luminatiProperties.getZone());
//             luminatiIPRequest.setCountry(luminatiProperties.getCountry());
//             luminatiIPRequest.setZonePassword(luminatiProperties.getProxyPassword());
//             luminatiIPRequest.setProxyUrl(luminatiProperties.getProxyHost());
//             luminatiIPRequest.setSocksPort(String.valueOf(port));
//
//             if (redisTemplate.hasKey("proxy:port:" + ipAddrDO.getIp())) {
//                 Integer portCache = (Integer)redisTemplate.opsForValue().get("proxy:port:" + ipAddrDO.getIp());
//                 killProxyByPort(String.valueOf(portCache));
//                 queue.add(portCache);
//             }
//             ExecuteCommandUtil.executeProxySps(luminatiIPRequest, ipAddrDO.getXLuminatiIp());
//             redisTemplate.opsForValue().set("proxy:port:" + ipAddrDO.getIp(), port);
//             return port;
//         } else {
//             throw new BizException(ReturnCode.RC999.getCode(), ReturnCode.RC999.getMessage());
//         }
//
//         // TODO 记录IP
//     }
//
//     @Override
//     public IpAddrDO luaGetIp(FindIpQuery findIpQuery) {
//         RLock rLock = redissonClient.getLock(RedisKeyConstant.LOCK_IP_KEY);
//         if (rLock.isLocked()) {
//             log.error("getIp获取锁失败:{}", RedisKeyConstant.LOCK_IP_KEY);
//             throw new BizException(ReturnCode.RC500.getCode(), "获取锁失败");
//         }
//         rLock.lock(5, TimeUnit.SECONDS);
//
//         if (CharSequenceUtil.isBlank(findIpQuery.getCountry())) {
//             findIpQuery.setCountry("us");
//         }
//
//         if (CharSequenceUtil.isNotBlank(findIpQuery.getCountry())) {
//             findIpQuery.setCountry(findIpQuery.getCountry().toLowerCase());
//         }
//         if (CharSequenceUtil.isNotBlank(findIpQuery.getState())) {
//             findIpQuery.setState(findIpQuery.getState().toLowerCase());
//         }
//         if (CharSequenceUtil.isNotBlank(findIpQuery.getCity())) {
//             findIpQuery.setCity(findIpQuery.getCity().toLowerCase());
//         }
//
//         IpAddrDO ipAddrDO = null;
//
//         if (CollUtil.isNotEmpty(findIpQuery.getZipCodeList())) {
//             List<ZipCodeQuery> zipCodeQueryList = findIpQuery.getZipCodeList();
//             List<String> zipCodeList =
//                 zipCodeQueryList.stream().sorted(Comparator.comparingDouble(ZipCodeQuery::getDistance))
//                     .map(ZipCodeQuery::getZipCode).collect(Collectors.toList());
//             List<IpAddrDO> ipAddrDOList = ipAddrMapper.selectList(new QueryWrapper<IpAddrDO>().lambda()
//                 .eq(IpAddrDO::getState, 1).eq(IpAddrDO::getUseState, 1).in(IpAddrDO::getPostalCode, zipCodeList));
//
//             if (CollUtil.isEmpty(ipAddrDOList)) {
//                 rLock.unlock();
//                 throw new BizException(ReturnCode.RC999.getCode(), "没有符合条件的IP");
//             }
//             ipAddrDO = ipAddrDOList.get(0);
//
//         } else {
//             long count = ipAddrMapper.selectCount(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getState, 1)
//                 .eq(IpAddrDO::getUseState, 1)
//                 .eq(CharSequenceUtil.isNotBlank(findIpQuery.getCountry()), IpAddrDO::getCountry,
//                     findIpQuery.getCountry())
//                 .eq(CharSequenceUtil.isNotBlank(findIpQuery.getState()), IpAddrDO::getRegion, findIpQuery.getState())
//                 .eq(CharSequenceUtil.isNotBlank(findIpQuery.getCity()), IpAddrDO::getCity, findIpQuery.getCity())
//                 .likeRight(CharSequenceUtil.isNotBlank(findIpQuery.getIp()), IpAddrDO::getIp, findIpQuery.getIp())
//                 .likeRight(CharSequenceUtil.isNotBlank(findIpQuery.getZipCode()), IpAddrDO::getPostalCode,
//                     findIpQuery.getZipCode()));
//
//             if (count > 0) {
//                 int c = RandomUtil.randomInt(0, (int)count);
//                 ipAddrDO = ipAddrMapper.selectOne(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getState, 1)
//                     .eq(IpAddrDO::getUseState, 1)
//                     .eq(CharSequenceUtil.isNotBlank(findIpQuery.getCountry()), IpAddrDO::getCountry,
//                         findIpQuery.getCountry())
//                     .eq(CharSequenceUtil.isNotBlank(findIpQuery.getState()), IpAddrDO::getRegion,
//                         findIpQuery.getState())
//                     .eq(CharSequenceUtil.isNotBlank(findIpQuery.getCity()), IpAddrDO::getCity, findIpQuery.getCity())
//                     .likeRight(CharSequenceUtil.isNotBlank(findIpQuery.getIp()), IpAddrDO::getIp, findIpQuery.getIp())
//                     .likeRight(CharSequenceUtil.isNotBlank(findIpQuery.getZipCode()), IpAddrDO::getPostalCode,
//                         findIpQuery.getZipCode())
//                     .last("limit " + c + " , 1"));
//             }
//         }
//
//         */
// /*IpAddrDO ipAddrDO = null;
//         if (CharSequenceUtil.isNotBlank(ip)) {
//             long ipAddrCount = ipAddrMapper.selectCount(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getState, 1)
//                 .eq(IpAddrDO::getUseState, 1).likeRight(IpAddrDO::getIp, ip));
//             if (ipAddrCount > 0) {
//                 int c = RandomUtil.randomInt(0, (int)ipAddrCount);
//                 ipAddrDO = ipAddrMapper.selectOne(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getState, 1)
//                     .eq(IpAddrDO::getUseState, 1).likeRight(IpAddrDO::getIp, ip).last("limit " + c + " , 1"));
//             }
//         }
//         if (ObjectUtil.isNull(ipAddrDO) && CharSequenceUtil.isNotBlank(zipCode)) {
//             long ipAddrCount = ipAddrMapper.selectCount(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getState, 1)
//                 .eq(IpAddrDO::getUseState, 1).likeRight(IpAddrDO::getPostalCode, zipCode));
//             if (ipAddrCount > 0) {
//                 int c = RandomUtil.randomInt(0, (int)ipAddrCount);
//                 ipAddrDO = ipAddrMapper.selectOne(
//                     new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getState, 1).eq(IpAddrDO::getUseState, 1)
//                         .likeRight(IpAddrDO::getPostalCode, zipCode).last("limit " + c + " , 1"));
//             }
//         }
//         if (ObjectUtil.isNull(ipAddrDO) && CharSequenceUtil.isNotBlank(state)) {
//             state = state.toLowerCase();
//             if (CharSequenceUtil.isNotBlank(city)) {
//                 city = city.toLowerCase();
//             }
//             long ipAddrCount = ipAddrMapper.selectCount(
//                 new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getState, 1).eq(IpAddrDO::getUseState, 1)
//                     .eq(IpAddrDO::getRegion, state).eq(CharSequenceUtil.isNotBlank(city), IpAddrDO::getCity, city));
//             if (ipAddrCount > 0) {
//                 int c = RandomUtil.randomInt(0, (int)ipAddrCount);
//                 ipAddrDO = ipAddrMapper.selectOne(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getState, 1)
//                     .eq(IpAddrDO::getUseState, 1).eq(IpAddrDO::getRegion, state)
//                     .eq(CharSequenceUtil.isNotBlank(city), IpAddrDO::getCity, city).last("limit " + c + " , 1"));
//             }
//         }*//*
//
//
//         if (ObjectUtil.isNull(ipAddrDO)) {
//             rLock.unlock();
//             throw new BizException(ReturnCode.RC500.getCode(), "没有符合的IP，请重试");
//         }
//         Boolean heartbeatResult = heartbeat(ipAddrDO.getId());
//         if (Boolean.FALSE.equals(heartbeatResult)) {
//             rLock.unlock();
//             throw new BizException(ReturnCode.RC500.getCode(), "IP测活失败");
//         }
//         if (Integer.valueOf(1).equals(ipAddrDO.getUseState())) {
//             IpAddrDO result = ipAddrMapper.selectById(ipAddrDO.getId());
//             result.setUseState(4);
//             ipAddrMapper.updateById(result);
//             rLock.unlock();
//             return result;
//         } else {
//             rLock.unlock();
//             throw new BizException(ReturnCode.RC500.getCode(), "没有符合的IP，请重试");
//         }
//     }
//
//     @Override
//     public void luaReportIp(Long id, Integer useState, String remark) {
//         // RLock lock = redissonClient.getLock(RedisKeyConstant.LOCK_IP_ID_KEY + id);
//         // try {
//         // lock.lock(5, TimeUnit.SECONDS);
//         ipAddrMapper.update(new IpAddrDO(),
//             new UpdateWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getId, id).set(IpAddrDO::getUseState, useState)
//                 .set(CharSequenceUtil.isNotBlank(remark), IpAddrDO::getRemark, remark));
//         // } finally {
//         // lock.unlock();
//         // }
//     }
//
//     @Override
//     public Boolean luaCheckIp(Long id) {
//         return heartbeat(id);
//     }
//
//     @Override
//     public LuminatiIPDTO checkXLuminatiIpAndIp(String xLuminatiIp, String ip) {
//         if (CharSequenceUtil.isBlank(xLuminatiIp)) {
//             throw new BizException(ReturnCode.RC500.getCode(), "xLuminatiIp不能为空");
//         }
//
//         String proxyUsername = luminatiProperties.getProxyUsername() + "-ip-" + xLuminatiIp;
//         try {
//             HttpResponse response = HttpRequest.get("http://lumtest.com/myip.json")
//                 .setProxy(new Proxy(Proxy.Type.HTTP,
//                     new InetSocketAddress(luminatiProperties.getProxyHost(), luminatiProperties.getProxyPort())))
//                 .basicProxyAuth(proxyUsername, luminatiProperties.getProxyPassword()).setReadTimeout(20000).execute();
//             log.info("Luminati HttpResponse:{}", response);
//
//             String luminatiResult = response.body();
//             LuminatiIPDTO luminatiIPDTO = JSON.parseObject(luminatiResult, LuminatiIPDTO.class);
//             if (CharSequenceUtil.isNotBlank(ip)) {
//                 if (CharSequenceUtil.equals(luminatiIPDTO.getIp(), ip)) {
//                     luminatiIPDTO.setState(1);
//                 } else {
//                     luminatiIPDTO.setState(0);
//                 }
//             }
//             return luminatiIPDTO;
//         } catch (Exception e) {
//             throw new BizException(ReturnCode.RC500.getCode(), "测活失败");
//         }
//     }
//
//     @Override
//     public void collect(LuminatiCollectIpRequest luminatiCollectIpRequest) {
//         // 判断 队列数量，>0拒绝任务
//         RBlockingQueue<LuminatiCollectIpRequest> queue =
//             redissonClient.getBlockingQueue(RedisKeyConstant.LUMINATI_COLLECT_IP_QUEUE_KEY);
//         if (queue.size() > 0) {
//             throw new BizException(ReturnCode.RC999.getCode(), "已有项目在执行，请等待完成后，再次增加收录项目。");
//         }
//
//         // 暂停，开始
//         RAtomicLong collectFlag = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_COLLECT_FLAG_KEY);
//         // 开始 10
//         collectFlag.set(10L);
//
//         // 当前任务适量
//         RAtomicLong currentNum = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_CURRENT_KEY);
//         currentNum.set(luminatiCollectIpRequest.getNumber());
//
//         // 放入队列
//         for (int i = 0; i < luminatiCollectIpRequest.getNumber(); i++) {
//             boolean result = queue.offer(luminatiCollectIpRequest);
//         }
//
//         redisTemplate.delete(RedisKeyConstant.LUMINATI_COLLECT_ERROR_KEY);
//     }
//
//     @Override
//     public LuminatiProgressVO collectProgress() {
//         RAtomicLong currentNum = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_CURRENT_KEY);
//
//         RAtomicLong totalNum = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_TOTAL_KEY);
//
//         String today = DateUtil.today();
//         RAtomicLong todayNum = redissonClient.getAtomicLong("#LUMINATI_" + today);
//
//         RBlockingQueue<RolaIpRequest> queue =
//             redissonClient.getBlockingQueue(RedisKeyConstant.LUMINATI_COLLECT_IP_QUEUE_KEY);
//
//         RAtomicLong currentRepeatNum = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_CURRENT_REPEAT_KEY);
//
//         RAtomicLong currentFailNum = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_CURRENT_FAIL_KEY);
//
//         String error = (String)redisTemplate.opsForValue().get(RedisKeyConstant.LUMINATI_COLLECT_ERROR_KEY);
//
//         LuminatiProgressVO luminatiProgressVO = new LuminatiProgressVO();
//         luminatiProgressVO.setCurrentNum(currentNum.get());
//         luminatiProgressVO.setTotalNum(totalNum.get());
//         luminatiProgressVO.setTodayNum(todayNum.get());
//         luminatiProgressVO.setCompletedNum(luminatiProgressVO.getCurrentNum() - queue.size());
//         luminatiProgressVO.setCurrentFailNum(currentFailNum.get());
//         luminatiProgressVO.setCurrentRepeatNum(currentRepeatNum.get());
//         luminatiProgressVO.setError(error);
//
//         return luminatiProgressVO;
//     }
//
//     @Override
//     public void endCollect() {
//         RAtomicLong collectFlag = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_COLLECT_FLAG_KEY);
//         collectFlag.set(11L);
//         RBlockingQueue<RolaIpRequest> queue =
//             redissonClient.getBlockingQueue(RedisKeyConstant.LUMINATI_COLLECT_IP_QUEUE_KEY);
//         queue.clear();
//     }
//
//     @Override
//     public void pauseCollect() {
//         RAtomicLong collectFlag = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_COLLECT_FLAG_KEY);
//         collectFlag.set(11L);
//     }
//
//     private void heartbeatFail(IpAddrDO ipAddrDO) {
//         if (ipAddrDO.getHeartbeatQuantity() >= 2) {
//             ipAddrDO.setState(0);
//             ipAddrDO.setHeartbeatQuantity(3);
//         } else {
//             ipAddrDO.setState(2);
//             ipAddrDO.setHeartbeatQuantity(ipAddrDO.getHeartbeatQuantity() + 1);
//         }
//         ipAddrDO.setLastHeartbeatTime(LocalDateTime.now());
//         ipAddrMapper.updateById(ipAddrDO);
//     }
//
//     private void saveOrUpdate(LuminatiIPDTO luminatiIPDTO, String xLuminatiIP) {
//         if (CharSequenceUtil.isAllNotBlank(xLuminatiIP, luminatiIPDTO.getIp(), luminatiIPDTO.getCountry(),
//             luminatiIPDTO.getGeo().getCity(), luminatiIPDTO.getGeo().getRegion(),
//             luminatiIPDTO.getGeo().getPostalCode())) {
//             long count = ipAddrMapper
//                 .selectCount(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getIp, luminatiIPDTO.getIp()));
//             if (count > 0) {
//                 log.info("已存在IP: {}，更新心跳时间", luminatiIPDTO.getIp());
//                 ipAddrMapper.update(new IpAddrDO(),
//                     new UpdateWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getIp, luminatiIPDTO.getIp())
//                         .set(IpAddrDO::getCountry, luminatiIPDTO.getCountry().toLowerCase())
//                         .set(IpAddrDO::getRegion, luminatiIPDTO.getGeo().getRegion().toLowerCase())
//                         .set(IpAddrDO::getCity, luminatiIPDTO.getGeo().getCity().toLowerCase())
//                         .set(IpAddrDO::getLastHeartbeatTime, LocalDateTime.now()));
//             } else {
//                 IpAddrDO ipAddrDO = new IpAddrDO();
//                 ipAddrDO.setIp(luminatiIPDTO.getIp());
//                 ipAddrDO.setCountry(luminatiIPDTO.getCountry().toLowerCase());
//                 ipAddrDO.setRegion(luminatiIPDTO.getGeo().getRegion().toLowerCase());
//                 ipAddrDO.setCity(luminatiIPDTO.getGeo().getCity().toLowerCase());
//                 ipAddrDO.setPostalCode(luminatiIPDTO.getGeo().getPostalCode());
//                 ipAddrDO.setXLuminatiIp(xLuminatiIP);
//                 ipAddrDO.setState(1);
//                 ipAddrDO.setLastHeartbeatTime(LocalDateTime.now());
//                 ipAddrDO.setCreateTime(LocalDateTime.now());
//                 ipAddrMapper.insert(ipAddrDO);
//                 log.info("插入新数据: {}", ipAddrDO);
//             }
//         }
//     }
// }
// */
