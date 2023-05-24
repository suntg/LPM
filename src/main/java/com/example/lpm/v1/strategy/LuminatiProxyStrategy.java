package com.example.lpm.v1.strategy;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.config.LuminatiProperties;
import com.example.lpm.constant.RedisKeyConstant;
import com.example.lpm.domain.dto.LuminatiIPDTO;
import com.example.lpm.v1.common.BizException;
import com.example.lpm.v1.common.ReturnCode;
import com.example.lpm.v1.constant.ProxyIpType;
import com.example.lpm.v1.constant.RedisLockKeyConstant;
import com.example.lpm.v1.domain.entity.AccountInfoDO;
import com.example.lpm.v1.domain.entity.ProxyIpDO;
import com.example.lpm.v1.domain.entity.ProxyPortDO;
import com.example.lpm.v1.domain.query.LuaGetProxyIpQuery;
import com.example.lpm.v1.domain.query.LuaZipCodeQuery;
import com.example.lpm.v1.domain.request.ChangeIpRequest;
import com.example.lpm.v1.domain.request.CheckIpSurvivalRequest;
import com.example.lpm.v1.domain.request.CollectionTaskRequest;
import com.example.lpm.v1.domain.request.StartProxyPortRequest;
import com.example.lpm.v1.service.AccountInfoService;
import com.example.lpm.v1.service.ProxyIpService;
import com.example.lpm.v1.service.ProxyPortService;
import com.example.lpm.v1.util.ExecuteCommandUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Deprecated
@Slf4j
@RequiredArgsConstructor
@Component
public class LuminatiProxyStrategy implements ProxyStrategy {

    private final RedissonClient redissonClient;

    private final ProxyIpService proxyIpService;

    private final LuminatiProperties luminatiProperties;

    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, String> redisTemplate;

    private final ProxyPortService proxyPortService;
    private final AccountInfoService accountInfoService;

    @Override
    public ProxyIpDO getProxyIp(LuaGetProxyIpQuery luaGetProxyIpQuery) {
        RLock rLock = redissonClient
            .getLock(RedisLockKeyConstant.LUA_GET_PROXY_IP_KEY + luaGetProxyIpQuery.getProxyIpType().getTypeName());
        if (rLock.isLocked()) {
            log.error("getIp获取锁失败:{}", RedisKeyConstant.LOCK_IP_KEY);
            throw new BizException(ReturnCode.RC500.getCode(), "获取锁失败");
        }
        rLock.lock(5, TimeUnit.SECONDS);

        try {
            if (CharSequenceUtil.isBlank(luaGetProxyIpQuery.getCountry())) {
                luaGetProxyIpQuery.setCountry("us");
            }
            if (CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getCountry())) {
                luaGetProxyIpQuery.setCountry(luaGetProxyIpQuery.getCountry().toLowerCase());
            }
            if (CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getState())) {
                luaGetProxyIpQuery.setState(luaGetProxyIpQuery.getState().toLowerCase());
            }
            if (CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getCity())) {
                luaGetProxyIpQuery.setCity(luaGetProxyIpQuery.getCity().toLowerCase());
            }

            ProxyIpDO proxyIpDO = null;

            if (CollUtil.isNotEmpty(luaGetProxyIpQuery.getZipCodeList())) {
                List<LuaZipCodeQuery> zipCodeQueryList = luaGetProxyIpQuery.getZipCodeList();
                List<String> zipCodeList =
                    zipCodeQueryList.stream().sorted(Comparator.comparingDouble(LuaZipCodeQuery::getDistance))
                        .map(LuaZipCodeQuery::getZipCode).collect(Collectors.toList());

                List<ProxyIpDO> proxyIpDOList = proxyIpService.list(new QueryWrapper<ProxyIpDO>().lambda()
                    .eq(ProxyIpDO::getStatus, 1).in(ProxyIpDO::getPostalCode, zipCodeList)
                    .eq(ProxyIpDO::getTypeName, luaGetProxyIpQuery.getProxyIpType())
                    .apply(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getFileType()),
                        "!json_contains(file_type, concat('\"" + luaGetProxyIpQuery.getFileType() + "\"'))"));

                if (CollUtil.isEmpty(proxyIpDOList)) {
                    throw new BizException(ReturnCode.RC999.getCode(), "没有符合条件的Luminati IP");
                }

                proxyIpDO = proxyIpDOList.get(0);
            } else {
                long count = proxyIpService.count(new QueryWrapper<ProxyIpDO>().lambda().eq(ProxyIpDO::getStatus, 1)
                    .eq(ProxyIpDO::getTypeName, luaGetProxyIpQuery.getProxyIpType())
                    .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getCountry()), ProxyIpDO::getCountry,
                        luaGetProxyIpQuery.getCountry())
                    .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getState()), ProxyIpDO::getRegion,
                        luaGetProxyIpQuery.getState())
                    .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getCity()), ProxyIpDO::getCity,
                        luaGetProxyIpQuery.getCity())
                    .likeRight(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getIp()), ProxyIpDO::getIp,
                        luaGetProxyIpQuery.getIp())
                    .likeRight(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getZipCode()), ProxyIpDO::getPostalCode,
                        luaGetProxyIpQuery.getZipCode())
                    .apply(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getFileType()),
                        "!json_contains(file_type, concat('\"" + luaGetProxyIpQuery.getFileType() + "\"'))"));
                if (count > 0) {
                    int c = RandomUtil.randomInt(0, (int)count);
                    proxyIpDO = proxyIpService.getOne(new QueryWrapper<ProxyIpDO>().lambda().eq(ProxyIpDO::getStatus, 1)
                        .eq(ProxyIpDO::getTypeName, luaGetProxyIpQuery.getProxyIpType())
                        .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getCountry()), ProxyIpDO::getCountry,
                            luaGetProxyIpQuery.getCountry())
                        .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getState()), ProxyIpDO::getRegion,
                            luaGetProxyIpQuery.getState())
                        .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getCity()), ProxyIpDO::getCity,
                            luaGetProxyIpQuery.getCity())
                        .likeRight(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getIp()), ProxyIpDO::getIp,
                            luaGetProxyIpQuery.getIp())
                        .likeRight(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getZipCode()),
                            ProxyIpDO::getPostalCode, luaGetProxyIpQuery.getZipCode())
                        .apply(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getFileType()),
                            "!json_contains(file_type, concat('\"" + luaGetProxyIpQuery.getFileType() + "\"'))")
                        .last("limit " + c + " , 1"));
                }
            }
            if (ObjectUtil.isNull(proxyIpDO)) {
                throw new BizException(ReturnCode.RC500.getCode(), "没有符合条件的Luminati IP，请重试");
            }
            Boolean heartbeatResult = heartbeat(proxyIpDO.getId());

            if (Boolean.FALSE.equals(heartbeatResult)) {
                throw new BizException(ReturnCode.RC500.getCode(), "Luminati IP测活失败");
            }
            proxyIpDO.setStatus(4);
            proxyIpService.updateById(proxyIpDO);

            return proxyIpDO;
        } finally {
            rLock.unlock();
        }

    }

    @Override
    public void checkIpSurvival(CheckIpSurvivalRequest checkIpSurvivalRequest) {
        ProxyIpDO proxyIpDO = proxyIpService.getOne(
            new QueryWrapper<ProxyIpDO>().lambda().eq(ProxyIpDO::getTypeName, checkIpSurvivalRequest.getProxyIpType())
                .eq(ProxyIpDO::getIp, checkIpSurvivalRequest.getIp()));

        if (proxyIpDO == null) {
            throw new BizException(900, "IP不存在");
        }

        if (CharSequenceUtil.isBlank(checkIpSurvivalRequest.getZone())) {
            // zzz_d10
            AccountInfoDO accountInfoDO = accountInfoService.getOne(new QueryWrapper<AccountInfoDO>().lambda()
                .eq(AccountInfoDO::getTypeName, checkIpSurvivalRequest.getProxyIpType().getTypeName())
                .eq(AccountInfoDO::getStatus, 1).eq(AccountInfoDO::getZone, "zzz_d10"));
            checkIpSurvivalRequest.setZone(accountInfoDO.getZone());

            if (CharSequenceUtil.isBlank(checkIpSurvivalRequest.getUsername())) {
                checkIpSurvivalRequest.setUsername(accountInfoDO.getUsername());

            }

            if (CharSequenceUtil.isBlank(checkIpSurvivalRequest.getPassword())) {
                checkIpSurvivalRequest.setPassword(accountInfoDO.getPassword());

            }
            if (CharSequenceUtil.isBlank(checkIpSurvivalRequest.getServer())) {
                checkIpSurvivalRequest.setServer(accountInfoDO.getServer());

            }
            if (CharSequenceUtil.isBlank(checkIpSurvivalRequest.getServerPort())) {
                checkIpSurvivalRequest.setServerPort(accountInfoDO.getServerPort());

            }

        } else {
            AccountInfoDO accountInfoDO = accountInfoService.getOne(new QueryWrapper<AccountInfoDO>().lambda()
                .eq(AccountInfoDO::getTypeName, checkIpSurvivalRequest.getProxyIpType().getTypeName())
                .eq(AccountInfoDO::getStatus, 1).eq(AccountInfoDO::getZone, checkIpSurvivalRequest.getZone()));
            if (CharSequenceUtil.isBlank(checkIpSurvivalRequest.getUsername())) {
                checkIpSurvivalRequest.setUsername(accountInfoDO.getUsername());
            }

            if (CharSequenceUtil.isBlank(checkIpSurvivalRequest.getPassword())) {
                checkIpSurvivalRequest.setPassword(accountInfoDO.getPassword());

            }
            if (CharSequenceUtil.isBlank(checkIpSurvivalRequest.getServer())) {
                checkIpSurvivalRequest.setServer(accountInfoDO.getServer());

            }
            if (CharSequenceUtil.isBlank(checkIpSurvivalRequest.getServerPort())) {
                checkIpSurvivalRequest.setServerPort(accountInfoDO.getServerPort());

            }
        }

        if (CharSequenceUtil.isBlank(checkIpSurvivalRequest.getXLuminatiIp())) {
            checkIpSurvivalRequest.setXLuminatiIp(proxyIpDO.getXLuminatiIp());

        }

        // brd-customer-c_41c902f3-zone-zzz_d10-ip-rbb5005d05f153f5202242f626bf534c8:7gocd4iw96ik

        String proxyUsername = "brd-customer-" + checkIpSurvivalRequest.getUsername() + "-zone-"
            + checkIpSurvivalRequest.getZone() + "-ip-" + checkIpSurvivalRequest.getXLuminatiIp();

        try {
            HttpResponse response = HttpRequest.get("http://lumtest.com/myip.json?ip=" + checkIpSurvivalRequest.getIp())
                .setProxy(new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(checkIpSurvivalRequest.getServer(),
                        Integer.parseInt(checkIpSurvivalRequest.getServerPort()))))
                .basicProxyAuth(proxyUsername, checkIpSurvivalRequest.getPassword()).setReadTimeout(20000).execute();
            log.info("Luminati HttpResponse:{}", response);

            String luminatiResult = response.body();

            LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(luminatiResult, LuminatiIPDTO.class);
            if (CharSequenceUtil.equals(luminatiIPDTO.getIp(), proxyIpDO.getIp())) {

            } else {
                proxyIpDO.setStatus(0);
                proxyIpService.updateById(proxyIpDO);
                throw new BizException(901, "IP不相同");
            }

        } catch (Exception e) {
            proxyIpDO.setStatus(0);
            proxyIpService.updateById(proxyIpDO);
            throw new BizException(ReturnCode.RC999.getCode(), "调用lumtest返回失败");
        }
    }

    @Override
    public boolean startProxyPort(StartProxyPortRequest startProxyPortRequest) {

        ProxyIpDO proxyIpDO = proxyIpService
            .getOne(new QueryWrapper<ProxyIpDO>().lambda().eq(ProxyIpDO::getIp, startProxyPortRequest.getProxyIp())
                .eq(ProxyIpDO::getTypeName, startProxyPortRequest.getProxyIpType()));
        if (proxyIpDO == null) {
            throw new BizException(ReturnCode.RC999.getCode(), "IP不存在");
        }

        long count = proxyPortService.count(
            new QueryWrapper<ProxyPortDO>().lambda().eq(ProxyPortDO::getProxyPort, startProxyPortRequest.getProxyPort())
                .eq(ProxyPortDO::getTypeName, startProxyPortRequest.getProxyIpType()));
        if (count > 0) {
            throw new BizException(997, "端口在使用中");
        }

        if (CharSequenceUtil.isBlank(startProxyPortRequest.getZone())) {
            // zzz_d10
            AccountInfoDO accountInfoDO = accountInfoService.getOne(new QueryWrapper<AccountInfoDO>().lambda()
                .eq(AccountInfoDO::getTypeName, startProxyPortRequest.getProxyIpType().getTypeName())
                .eq(AccountInfoDO::getStatus, 1).eq(AccountInfoDO::getZone, "zzz_d10"));
            startProxyPortRequest.setZone(accountInfoDO.getZone());

            if (CharSequenceUtil.isBlank(startProxyPortRequest.getUsername())) {
                startProxyPortRequest.setUsername(accountInfoDO.getUsername());

            }

            if (CharSequenceUtil.isBlank(startProxyPortRequest.getPassword())) {
                startProxyPortRequest.setPassword(accountInfoDO.getPassword());

            }
            if (CharSequenceUtil.isBlank(startProxyPortRequest.getServer())) {
                startProxyPortRequest.setServer(accountInfoDO.getServer());

            }
            if (CharSequenceUtil.isBlank(startProxyPortRequest.getServerPort())) {
                startProxyPortRequest.setServerPort(accountInfoDO.getServerPort());

            }

        } else {
            AccountInfoDO accountInfoDO = accountInfoService.getOne(new QueryWrapper<AccountInfoDO>().lambda()
                .eq(AccountInfoDO::getTypeName, startProxyPortRequest.getProxyIpType().getTypeName())
                .eq(AccountInfoDO::getStatus, 1).eq(AccountInfoDO::getZone, startProxyPortRequest.getZone()));
            if (CharSequenceUtil.isBlank(startProxyPortRequest.getUsername())) {
                startProxyPortRequest.setUsername(accountInfoDO.getUsername());
            }

            if (CharSequenceUtil.isBlank(startProxyPortRequest.getPassword())) {
                startProxyPortRequest.setPassword(accountInfoDO.getPassword());

            }
            if (CharSequenceUtil.isBlank(startProxyPortRequest.getServer())) {
                startProxyPortRequest.setServer(accountInfoDO.getServer());

            }
            if (CharSequenceUtil.isBlank(startProxyPortRequest.getServerPort())) {
                startProxyPortRequest.setServerPort(accountInfoDO.getServerPort());

            }
        }

        if (CharSequenceUtil.isBlank(startProxyPortRequest.getXLuminatiIp())) {
            startProxyPortRequest.setXLuminatiIp(proxyIpDO.getXLuminatiIp());

        }

        String proxyUsername = "brd-customer-" + startProxyPortRequest.getUsername() + "-zone-"
            + startProxyPortRequest.getZone() + "-ip-" + startProxyPortRequest.getXLuminatiIp();

        try {
            HttpResponse response =
                HttpRequest.get("http://lumtest.com/myip.json?ip=" + startProxyPortRequest.getProxyIp())
                    .setProxy(new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(startProxyPortRequest.getServer(),
                            Integer.parseInt(startProxyPortRequest.getServerPort()))))
                    .basicProxyAuth(proxyUsername, startProxyPortRequest.getPassword()).setReadTimeout(20000).execute();
            log.info("Luminati HttpResponse:{}", response);

            String luminatiResult = response.body();

            LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(luminatiResult, LuminatiIPDTO.class);
            if (CharSequenceUtil.equals(luminatiIPDTO.getIp(), proxyIpDO.getIp())) {

            } else {
                proxyIpDO.setStatus(0);
                proxyIpService.updateById(proxyIpDO);
                throw new BizException(901, "IP不相同");
            }

        } catch (Exception e) {
            proxyIpDO.setStatus(0);
            proxyIpService.updateById(proxyIpDO);
            throw new BizException(ReturnCode.RC999.getCode(), "调用lumtest返回失败");
        }

        String username = "http://lum-customer-" + startProxyPortRequest.getUsername() + "-zone-"
            + startProxyPortRequest.getZone() + "-dns-remote-ip-" + startProxyPortRequest.getXLuminatiIp();

        ExecuteCommandUtil.executeLumProxySps(startProxyPortRequest.getProxyPort(),
            startProxyPortRequest.getProxyUsername(), startProxyPortRequest.getProxyPassword(), username,
            startProxyPortRequest.getPassword(), startProxyPortRequest.getServer(),
            startProxyPortRequest.getServerPort());

        ProxyPortDO proxyPortDO = new ProxyPortDO();
        proxyPortDO.setProxyPort(startProxyPortRequest.getProxyPort());
        proxyPortDO.setIp(startProxyPortRequest.getProxyIp());
        proxyPortDO.setName(startProxyPortRequest.getDeviceName());
        proxyPortDO.setTypeName(startProxyPortRequest.getProxyIpType());
        proxyPortService.save(proxyPortDO);
        return true;
    }

    @Override
    public void addCollectionTask(CollectionTaskRequest collectionTaskRequest) {
        // 判断 队列数量，>0拒绝任务
        RBlockingQueue<CollectionTaskRequest> queue =
            redissonClient.getBlockingQueue(com.example.lpm.v1.constant.RedisKeyConstant.COLLECTION_TASK_TOPIC
                + collectionTaskRequest.getProxyIpType().getTypeName());

        if (queue.size() > 0) {
            throw new BizException(ReturnCode.RC999.getCode(),
                collectionTaskRequest.getProxyIpType().getTypeName() + "已有项目在执行，请等待完成后，再次增加收录项目。");
        }

        // 暂停，开始
        RAtomicLong collectFlag =
            redissonClient.getAtomicLong(com.example.lpm.v1.constant.RedisKeyConstant.COLLECTION_TASK_FLAG
                + collectionTaskRequest.getProxyIpType().getTypeName());
        // 开始 10
        collectFlag.set(10L);

        // 当前任务适量
        RAtomicLong currentNum =
            redissonClient.getAtomicLong(com.example.lpm.v1.constant.RedisKeyConstant.COLLECTION_TASK_CURRENT
                + collectionTaskRequest.getProxyIpType().getTypeName());
        currentNum.set(collectionTaskRequest.getNumber());

        // 放入队列
        for (int i = 0; i < collectionTaskRequest.getNumber(); i++) {
            queue.offerAsync(collectionTaskRequest);
        }

        redisTemplate.delete(com.example.lpm.v1.constant.RedisKeyConstant.COLLECTION_TASK_ERROR
            + collectionTaskRequest.getProxyIpType().getTypeName());
    }

    @Override
    public void getCollectionProgress() {

    }

    @Override
    public void changeProxyIp(ChangeIpRequest changeIpRequest) {

    }

    public Boolean heartbeat(Long id) {
        ProxyIpDO proxyIpDO = proxyIpService.getOne(new QueryWrapper<ProxyIpDO>().lambda().eq(ProxyIpDO::getId, id));

        String proxyUsername = luminatiProperties.getProxyUsername() + "-ip-" + proxyIpDO.getXLuminatiIp();
        log.info("heartbeat param id:{}, ip:{}, x-luminati-ip:{}", id, proxyIpDO.getIp(), proxyIpDO.getXLuminatiIp());
        try {
            HttpResponse response = HttpRequest.get("http://lumtest.com/myip.json?ip=" + proxyIpDO.getIp())
                .setProxy(new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(luminatiProperties.getProxyHost(), luminatiProperties.getProxyPort())))
                .basicProxyAuth(proxyUsername, luminatiProperties.getProxyPassword()).setReadTimeout(20000).execute();
            log.info("Luminati HttpResponse:{}", response);

            String luminatiResult = response.body();

            LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(luminatiResult, LuminatiIPDTO.class);
            if (CharSequenceUtil.equals(luminatiIPDTO.getIp(), proxyIpDO.getIp())) {
                proxyIpDO.setStatus(1);
                proxyIpService.updateById(proxyIpDO);
                return Boolean.TRUE;
            } else {
                // 如果不相等
                proxyIpDO.setStatus(0);
                proxyIpService.updateById(proxyIpDO);
                return Boolean.FALSE;
            }

        } catch (Exception e) {
            log.error("heartbeatFail 异常: {}", ExceptionUtil.stacktraceToString(e));

            proxyIpDO.setStatus(0);
            proxyIpService.updateById(proxyIpDO);
            return Boolean.FALSE;
        }
    }

    @Override
    public ProxyIpType getStrategyName() {
        return ProxyIpType.LUMINATI;
    }
}
