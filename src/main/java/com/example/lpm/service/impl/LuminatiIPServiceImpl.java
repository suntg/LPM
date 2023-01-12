package com.example.lpm.service.impl;

import static com.example.lpm.util.ExecuteCommandUtil.executeProxySps;
import static com.example.lpm.util.ExecuteCommandUtil.killProxyByPort;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.lpm.v3.common.ReturnCode;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.constant.RedisKeyConstant;
import com.example.lpm.domain.dto.*;
import com.example.lpm.domain.entity.IpAddrDO;
import com.example.lpm.domain.entity.LuminatiIPDO;
import com.example.lpm.domain.entity.ProxyServerInfoDO;
import com.example.lpm.domain.entity.UsedPortDO;
import com.example.lpm.domain.request.DeleteProxyPortRequest;
import com.example.lpm.domain.request.LuminatiIPRequest;
import com.example.lpm.domain.request.LuminatiProxyRequest;
import com.example.lpm.mapper.IpAddrMapper;
import com.example.lpm.mapper.LuminatiIPMapper;
import com.example.lpm.mapper.ProxyServerInfoMapper;
import com.example.lpm.mapper.UsedPortMapper;
import com.example.lpm.service.IpAddrService;
import com.example.lpm.service.LuminatiIPService;
import com.example.lpm.util.ExecuteCommandUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LuminatiIPServiceImpl implements LuminatiIPService {

    @Resource
    private LuminatiIPMapper luminatiIPMapper;
    @Resource
    private UsedPortMapper usedPortMapper;
    @Resource
    private ProxyServerInfoMapper proxyServerInfoMapper;
    @Resource
    private IpAddrService ipAddrService;
    @Resource
    private IpAddrMapper ipAddrMapper;
    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LuminatiIPDO getIPAndStartProxy(LuminatiIPRequest luminatiIPRequest) {
        log.info("LuminatiIP Request:{}", luminatiIPRequest);
        if (CharSequenceUtil.hasBlank(luminatiIPRequest.getSocksPort(), luminatiIPRequest.getProxyUrl())) {
            throw new BizException(ReturnCode.RC500.getCode(), ReturnCode.RC500.getMessage());
        }
        // 大于0 查看端口占用数量
        if (luminatiIPRequest.getSocksPortCount() > 0) {
            int portCount = getProxyPortCount();
            log.info("目前占用端口数量:{}", portCount);
            if (portCount >= luminatiIPRequest.getSocksPortCount()) {
                throw new BizException(ReturnCode.RC500.getCode(), "目前占用端口数量" + portCount + "个，超过限制");
            }
        }
        // 如果有token x-luminati-ip， ip，请求http://lumtest.com/myip.json后进行校验ip，如果不同返回失败
        if (CharSequenceUtil.isAllNotEmpty(luminatiIPRequest.getXLuminatiIp(), luminatiIPRequest.getIp())) {
            // sps
            // lum-customer-c_99c3c376-zone-zone5-dns-remote-route_err-pass_dyn-country-us-ip-r2cf79644c5a500f5c56ece571206badf
            String proxyUsername =
                CharSequenceUtil.format("lum-customer-{}-zone-{}-dns-remote-route_err-pass_dyn-country-{}-ip-{}",
                    luminatiIPRequest.getCustomer(), luminatiIPRequest.getZone(), luminatiIPRequest.getCountry(),
                    luminatiIPRequest.getXLuminatiIp());
            try {
                HttpResponse response = HttpRequest.get("http://lumtest.com/myip.json")
                    .setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(luminatiIPRequest.getProxyUrl(), 22225)))
                    .basicProxyAuth(proxyUsername, luminatiIPRequest.getZonePassword()).setReadTimeout(20000).execute();
                String xLuminatiIP = response.header("x-luminati-ip");
                String luminatiResult = response.body();
                LuminatiIPDTO luminatiIPDTO = JSON.parseObject(luminatiResult, LuminatiIPDTO.class);
                if (CharSequenceUtil.equalsIgnoreCase(luminatiIPRequest.getIp(), luminatiIPDTO.getIp())) {
                    LuminatiIPDO luminatiIPDO = buildLuminatiIPDO(luminatiIPRequest, xLuminatiIP, luminatiIPDTO);
                    luminatiIPMapper.insert(luminatiIPDO);

                    killProxyByPort(luminatiIPRequest.getSocksPort());

                    executeProxySps(luminatiIPRequest, luminatiIPDO.getXLuminatiIp());

                    return luminatiIPDO;
                } else {
                    throw new BizException(ReturnCode.RC500.getCode(), "IP不相等");
                }
            } catch (Exception e) {
                throw new BizException(ReturnCode.RC500.getCode(), "x-luminati-ip对应IP异常");
            }
        }
        killProxyByPort(luminatiIPRequest.getSocksPort());

        LuminatiIPDO luminatiIPDO = getLuminatiIPDO(luminatiIPRequest);

        if (luminatiIPRequest.getCheckIPTime() > 0) {
            LuminatiIPDO nonexistentluminatiIPDO = getNonexistentIp(luminatiIPRequest, luminatiIPDO);
            luminatiIPMapper.insert(nonexistentluminatiIPDO);
            ExecuteCommandUtil.executeProxySps(luminatiIPRequest, nonexistentluminatiIPDO.getXLuminatiIp());
            nonexistentluminatiIPDO.setSocksPort(luminatiIPRequest.getSocksPort());
            return nonexistentluminatiIPDO;
        } else {
            luminatiIPMapper.insert(luminatiIPDO);
            ExecuteCommandUtil.executeProxySps(luminatiIPRequest, luminatiIPDO.getXLuminatiIp());
            luminatiIPDO.setSocksPort(luminatiIPRequest.getSocksPort());
            return luminatiIPDO;
        }
    }

    private LuminatiIPDO buildLuminatiIPDO(LuminatiIPRequest luminatiIPRequest, String xLuminatiIP,
        LuminatiIPDTO luminatiIPDTO) {
        LuminatiIPDO luminatiIPDO = new LuminatiIPDO();
        luminatiIPDO.setXLuminatiIp(xLuminatiIP);
        luminatiIPDO.setIp(luminatiIPDTO.getIp());
        luminatiIPDO.setCountry(luminatiIPDTO.getCountry());
        luminatiIPDO.setRegion(luminatiIPDTO.getGeo().getRegion());
        luminatiIPDO.setCity(luminatiIPDTO.getGeo().getCity());
        luminatiIPDO.setPostalCode(luminatiIPDTO.getGeo().getPostalCode());
        luminatiIPDO.setSocksPort(luminatiIPRequest.getSocksPort());
        luminatiIPDO.setSocksUsername(luminatiIPRequest.getSocksUsername());
        luminatiIPDO.setSocksPassword(luminatiIPRequest.getSocksPassword());
        luminatiIPDO.setCustomer(luminatiIPRequest.getCustomer());
        luminatiIPDO.setZone(luminatiIPRequest.getZone());
        luminatiIPDO.setZonePassword(luminatiIPRequest.getZonePassword());
        luminatiIPDO.setProxyUrl(luminatiIPRequest.getProxyUrl());
        return luminatiIPDO;
    }

    @Override
    public void stopProxyByPort(String port) {
        if (CharSequenceUtil.equalsIgnoreCase("all", port)) {
            // killall -9 proxy
            String result = RuntimeUtil.execForStr("killall -9 proxy");
            log.info("kill all proxy:{}", result);
        } else {
            killProxyByPort(port);
        }

    }

    private LuminatiIPDO getNonexistentIp(LuminatiIPRequest luminatiIPRequest, LuminatiIPDO luminatiIPDO) {
        long count = luminatiIPMapper.selectCount(new QueryWrapper<LuminatiIPDO>().lambda()
            .eq(LuminatiIPDO::getIp, luminatiIPDO.getIp()).ge(LuminatiIPDO::getCreateTime, LocalDateTimeUtil
                .offset(LocalDateTime.now(), -luminatiIPRequest.getCheckIPTime(), ChronoUnit.MINUTES)));
        if (count > 0) {
            getNonexistentIp(luminatiIPRequest, getLuminatiIPDO(luminatiIPRequest));
        }
        return luminatiIPDO;
    }

    private LuminatiIPDO getLuminatiIPDO(LuminatiIPRequest luminatiIPRequest) {

        // lum-customer-{}-zone-{}-dns-remote-route_err-pass_dyn-country-{}
        String proxyUsername =
            CharSequenceUtil.format("lum-customer-{}-zone-{}-dns-remote-route_err-pass_dyn-country-{}",
                luminatiIPRequest.getCustomer(), luminatiIPRequest.getZone(), luminatiIPRequest.getCountry());

        int proxyPort = 22225;
        HttpResponse response = HttpRequest.get("http://lumtest.com/myip.json")
            .setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(luminatiIPRequest.getProxyUrl(), proxyPort)))
            .basicProxyAuth(proxyUsername, luminatiIPRequest.getZonePassword()).execute();
        log.info("Luminati HttpResponse:{}", response);

        String xLuminatiIP = response.header("x-luminati-ip");
        String luminatiResult = response.body();
        LuminatiIPDTO luminatiIPDTO = JSON.parseObject(luminatiResult, LuminatiIPDTO.class);

        if (!CharSequenceUtil.equalsIgnoreCase(luminatiIPDTO.getCountry(), luminatiIPRequest.getCountry())) {
            getLuminatiIPDO(luminatiIPRequest);
        }
        return buildLuminatiIPDO(luminatiIPRequest, xLuminatiIP, luminatiIPDTO);
    }

    private int getProxyPortCount() {
        List<String> resultList = RuntimeUtil.execForLines("ps -e");
        int count = 0;
        for (String s : resultList) {
            if (CharSequenceUtil.containsIgnoreCase(s, "proxy")) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void getProxyPort(LuminatiProxyRequest luminatiProxyRequest) {
        // proxyServer 从机器表中获取

        List<ProxyServerInfoDO> proxyServerInfoList = proxyServerInfoMapper.selectList(new QueryWrapper<>());
        ProxyServerInfoDO proxyServerInfo = proxyServerInfoList.get(0);
        if (CharSequenceUtil.isBlank(luminatiProxyRequest.getProxyServer())) {
            luminatiProxyRequest.setProxyServer(proxyServerInfoList.get(0).getServerIp());
        }
        RLock lock = redissonClient.getLock(RedisKeyConstant.START_PROXY_KEY + proxyServerInfo.getId());

        lock.lock(5, TimeUnit.SECONDS);
        try {
            // proxyPort 为空则随机
            long usedCount = usedPortMapper.selectCount(
                new QueryWrapper<UsedPortDO>().lambda().eq(UsedPortDO::getServerId, proxyServerInfo.getId()));
            if (usedCount >= proxyServerInfo.getPortNumLimit()) {
                // 超过总量限制
                throw new BizException(ReturnCode.RC500.getCode(), ReturnCode.RC500.getMessage());

            }
            if (ObjectUtil.isNotNull(luminatiProxyRequest.getProxyPort())) {
                if (luminatiProxyRequest.getDeleteProxyPortFlag() < 1) {
                    long count = usedPortMapper.selectCount(
                        new QueryWrapper<UsedPortDO>().lambda().eq(UsedPortDO::getServerId, proxyServerInfo.getId())
                            .eq(UsedPortDO::getServerPort, luminatiProxyRequest.getProxyPort()));
                    // 如果已经存在
                    if (count > 0) {
                        throw new BizException(ReturnCode.RC500.getCode(), "端口已经使用");
                    }
                }
            } else {
                // 随机一个端口
                Integer maxPort = usedPortMapper.selectMaxPort(proxyServerInfo.getId());
                if (ObjectUtil.isNotNull(maxPort)) {
                    luminatiProxyRequest.setProxyPort(maxPort + 1);
                } else {
                    luminatiProxyRequest.setProxyPort(40000);
                }
            }
            // x-lumianti-ip 随机在测活API接口随机取一个IP出来，需要是测活过的
            // 若不为空情况下啊 x-lumianti-ip GetIP 值都不可以为空， 建立端口需要测活并且验证该IP地址是否正确
            if (CharSequenceUtil.isBlank(luminatiProxyRequest.getXLuminatiIp())) {
                // x-lumianti-ip 为空，随机一个测活ip
                long ipAddrCount =
                    ipAddrMapper.selectCount(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getState, 1)
                        .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getCountry()), IpAddrDO::getCountry,
                            luminatiProxyRequest.getCountry())
                        .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getState()), IpAddrDO::getRegion,
                            luminatiProxyRequest.getState())
                        .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getCity()), IpAddrDO::getCity,
                            luminatiProxyRequest.getCity())
                        .likeRight(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getZipCode()),
                            IpAddrDO::getPostalCode, luminatiProxyRequest.getZipCode()));
                int c = RandomUtil.randomInt(0, (int)ipAddrCount);
                IpAddrDO ipAddrDO =
                    ipAddrMapper.selectOne(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getState, 1)
                        .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getCountry()), IpAddrDO::getCountry,
                            luminatiProxyRequest.getCountry())
                        .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getState()), IpAddrDO::getRegion,
                            luminatiProxyRequest.getState())
                        .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getCity()), IpAddrDO::getCity,
                            luminatiProxyRequest.getCity())
                        .likeRight(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getZipCode()),
                            IpAddrDO::getPostalCode, luminatiProxyRequest.getZipCode())
                        .last("limit " + c + " , 1"));
                // 接口测活
                Boolean heartbeatResult = ipAddrService.heartbeat(ipAddrDO.getId());
                if (Boolean.FALSE.equals(heartbeatResult)) {
                    throw new BizException(ReturnCode.RC500.getCode(), "IP测活失败");
                }

                extracted(luminatiProxyRequest, proxyServerInfo, ipAddrDO);
            } else {
                // 如果x-lumianti-ip不为空，checkip也不为空
                if (CharSequenceUtil.isBlank(luminatiProxyRequest.getCheckIp())) {
                    throw new BizException(ReturnCode.RC500.getCode(), "CheckIP不能为空");
                } else {
                    long ipAddrCount =
                        ipAddrMapper.selectCount(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getState, 1)
                            .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getXLuminatiIp()),
                                IpAddrDO::getXLuminatiIp, luminatiProxyRequest.getXLuminatiIp())
                            .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getCountry()), IpAddrDO::getCountry,
                                luminatiProxyRequest.getCountry())
                            .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getState()), IpAddrDO::getRegion,
                                luminatiProxyRequest.getState())
                            .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getCity()), IpAddrDO::getCity,
                                luminatiProxyRequest.getCity())
                            .likeRight(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getZipCode()),
                                IpAddrDO::getPostalCode, luminatiProxyRequest.getZipCode()));

                    int c = RandomUtil.randomInt(0, (int)ipAddrCount);

                    IpAddrDO ipAddrDO = ipAddrMapper.selectOne(new QueryWrapper<IpAddrDO>().lambda()
                        .eq(IpAddrDO::getState, 1)
                        .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getXLuminatiIp()),
                            IpAddrDO::getXLuminatiIp, luminatiProxyRequest.getXLuminatiIp())
                        .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getCountry()), IpAddrDO::getCountry,
                            luminatiProxyRequest.getCountry())
                        .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getState()), IpAddrDO::getRegion,
                            luminatiProxyRequest.getState())
                        .eq(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getCity()), IpAddrDO::getCity,
                            luminatiProxyRequest.getCity())
                        .likeRight(CharSequenceUtil.isNotBlank(luminatiProxyRequest.getZipCode()),
                            IpAddrDO::getPostalCode, luminatiProxyRequest.getZipCode())
                        .last("limit " + c + " , 1"));

                    if (ObjectUtil.isNull(ipAddrDO)) {
                        throw new BizException(ReturnCode.RC500.getCode(), "数据库没有可用IP");
                    }
                    // 接口测活
                    Boolean heartbeatResult = ipAddrService.heartbeat(ipAddrDO.getId());
                    if (Boolean.FALSE.equals(heartbeatResult)) {
                        throw new BizException(ReturnCode.RC500.getCode(), "IP测活失败");
                    }
                    if (!CharSequenceUtil.equals(luminatiProxyRequest.getCheckIp(), ipAddrDO.getIp())) {
                        throw new BizException(ReturnCode.RC500.getCode(), "CheckIp和数据库中ip不等");
                    }

                    extracted(luminatiProxyRequest, proxyServerInfo, ipAddrDO);

                }
            }
        } finally {
            lock.unlock();
        }

    }

    @Override
    @Transactional
    public void deleteProxyPort(DeleteProxyPortRequest deleteProxyPortRequest) {
        ProxyServerInfoDO proxyServerInfoDO = proxyServerInfoMapper.selectOne(new QueryWrapper<ProxyServerInfoDO>()
            .lambda().eq(ProxyServerInfoDO::getServerIp, deleteProxyPortRequest.getServer()));
        if (proxyServerInfoDO != null) {
            if (CollUtil.isEmpty(deleteProxyPortRequest.getPorts())) {
                List<UsedPortDO> usedPortDOList = usedPortMapper.selectList(
                    new QueryWrapper<UsedPortDO>().lambda().eq(UsedPortDO::getServerId, proxyServerInfoDO.getId()));
                List<Integer> list = new ArrayList<>();
                for (UsedPortDO usedPortDO : usedPortDOList) {
                    list.add(usedPortDO.getServerPort());
                }
                deleteProxyPortRequest.setPorts(list);
            }
            excuteDeletePort(deleteProxyPortRequest.getPorts(), deleteProxyPortRequest.getServer(),
                proxyServerInfoDO.getApiPort());
            for (Integer port : deleteProxyPortRequest.getPorts()) {
                usedPortMapper.delete(new UpdateWrapper<UsedPortDO>().lambda()
                    .eq(UsedPortDO::getServerId, proxyServerInfoDO.getId()).eq(UsedPortDO::getServerPort, port));
            }
        }
    }

    @Override
    public JSONArray stateProxyPort() {
        List<ProxyServerInfoDO> proxyServerInfoList = proxyServerInfoMapper.selectList(new QueryWrapper<>());
        ProxyServerInfoDO proxyServerInfo = proxyServerInfoList.get(0);
        return excuteStateProxyPort(proxyServerInfo.getServerIp(), proxyServerInfo.getApiPort());
    }

    private void extracted(LuminatiProxyRequest luminatiProxyRequest, ProxyServerInfoDO proxyServerInfo,
        IpAddrDO ipAddrDO) {
        if (luminatiProxyRequest.getDeleteProxyPortFlag() > 0) {
            // 删除端口
            List<Integer> ports = new ArrayList<>();
            ports.add(luminatiProxyRequest.getProxyPort());
            DeleteProxyPortRequest deleteProxyPortRequest = new DeleteProxyPortRequest();
            deleteProxyPortRequest.setServer(proxyServerInfo.getServerIp());
            deleteProxyPortRequest.setPorts(ports);
            deleteProxyPort(deleteProxyPortRequest);
        }
        if (!proxyServerInfo.getApiPort().equals(luminatiProxyRequest.getApiPort())) {
            proxyServerInfo.setApiPort(luminatiProxyRequest.getApiPort());
            proxyServerInfoMapper.updateById(proxyServerInfo);
        }

        excuteCreatePort(ipAddrDO.getXLuminatiIp(), luminatiProxyRequest.getProxyPort(), proxyServerInfo.getServerIp(),
            luminatiProxyRequest.getZone(), luminatiProxyRequest.getZonePassword(), luminatiProxyRequest.getApiPort());

        // 更新
        UsedPortDO usedPortDO = new UsedPortDO();
        usedPortDO.setServerPort(luminatiProxyRequest.getProxyPort());
        usedPortDO.setCreateTime(LocalDateTime.now());
        usedPortDO.setIpAddrId(ipAddrDO.getId());
        usedPortDO.setServerId(proxyServerInfo.getId());
        usedPortMapper.insert(usedPortDO);

        if (luminatiProxyRequest.getTimeout() != null && luminatiProxyRequest.getTimeout() > 0) {
            RBlockingQueue<ProxyDelayDTO> blockingQueue =
                redissonClient.getBlockingQueue(RedisKeyConstant.START_PROXY_DELAYED_QUEUE_KEY);
            RDelayedQueue<ProxyDelayDTO> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);

            ProxyDelayDTO proxyDelayDTO = new ProxyDelayDTO();
            proxyDelayDTO.setServerId(proxyServerInfo.getId());
            proxyDelayDTO.setServerPort(luminatiProxyRequest.getProxyPort());
            delayedQueue.offer(proxyDelayDTO, luminatiProxyRequest.getTimeout(), TimeUnit.SECONDS);
            delayedQueue.destroy();
        }
    }

    private void excuteCreatePort(String ip, Integer port, String server, String zone, String zonePassword,
        Integer apiPort) {

        ProxyAddDTO proxyAddDTO = new ProxyAddDTO();
        proxyAddDTO.setPort(port);
        LuminatiAddDTO luminatiAddDTO = new LuminatiAddDTO();
        luminatiAddDTO.setProxy(proxyAddDTO);

        String luminatiAddDTOJson = JSON.toJSONString(luminatiAddDTO);
        log.info("luminatiAddDTOJson: {}", luminatiAddDTOJson);

        HttpResponse response = HttpRequest.post("http://" + server + ":" + apiPort + "/api/proxies")
            .body(luminatiAddDTOJson).setReadTimeout(20000).execute();
        log.info("Proxy Manager /api/proxies post response : {}", response.toString());
        if (response.body().contains("errors")) {
            throw new BizException(ReturnCode.RC500.getCode(), "创建端口接口异常");
        }

        ProxyUpdateDTO proxyUpdateDTO = new ProxyUpdateDTO();
        proxyUpdateDTO.setIp(ip);
        if (CharSequenceUtil.isAllNotBlank(zone, zonePassword)) {
            proxyUpdateDTO.setZone(zone);
            proxyUpdateDTO.setPassword(zonePassword);
        }
        LuminatiUpdateDTO luminatiUpdateDTO = new LuminatiUpdateDTO();
        luminatiUpdateDTO.setProxy(proxyUpdateDTO);

        String luminatiUpdateDTOJson = JSON.toJSONString(luminatiUpdateDTO);
        log.info("luminatiUpdateDTOJson: {}", luminatiUpdateDTOJson);
        response = HttpRequest.put("http://" + server + ":" + apiPort + "/api/proxies/" + port)
            .body(luminatiUpdateDTOJson).setReadTimeout(20000).execute();
        log.info("Proxy Manager /api/proxies put response : {}", response.toString());
        if (response.body().contains("errors")) {
            throw new BizException(ReturnCode.RC500.getCode(), "更新端口接口异常");
        }
    }

    private void excuteDeletePort(List<Integer> ports, String server, Integer apiPort) {
        ProxyDTO proxyDTO = new ProxyDTO();
        proxyDTO.setPorts(ports);
        HttpResponse response = HttpRequest.post("http://" + server + ":" + apiPort + "/api/proxies/delete")
            .body(JSON.toJSONString(proxyDTO)).setReadTimeout(20000).execute();
        if (response.body().contains("errors")) {
            throw new BizException(ReturnCode.RC500.getCode(), "更新端口接口异常");
        }
    }

    private JSONArray excuteStateProxyPort(String server, Integer apiPort) {
        HttpResponse response = HttpRequest.get("http://" + server + ":" + apiPort + "/api/proxies_running")
            .setReadTimeout(20000).execute();
        return JSON.parseArray(response.body());
    }

}
