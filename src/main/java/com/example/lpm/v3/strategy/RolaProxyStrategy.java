package com.example.lpm.v3.strategy;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
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
import com.example.lpm.constant.ProxyConstant;
import com.example.lpm.domain.dto.LuminatiIPDTO;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.v3.common.ReturnCode;
import com.example.lpm.v3.config.GzipRequestInterceptor;
import com.example.lpm.v3.constant.ProxyIpType;
import com.example.lpm.v3.constant.RedisKeyConstant;
import com.example.lpm.v3.constant.RedisLockKeyConstant;
import com.example.lpm.v3.domain.entity.AccountInfoDO;
import com.example.lpm.v3.domain.entity.ProxyIpDO;
import com.example.lpm.v3.domain.entity.ProxyPortDO;
import com.example.lpm.v3.domain.query.LuaGetProxyIpQuery;
import com.example.lpm.v3.domain.query.LuaZipCodeQuery;
import com.example.lpm.v3.domain.request.CheckIpSurvivalRequest;
import com.example.lpm.v3.domain.request.CollectionTaskRequest;
import com.example.lpm.v3.domain.request.StartProxyPortRequest;
import com.example.lpm.v3.service.AccountInfoService;
import com.example.lpm.v3.service.ProxyIpService;
import com.example.lpm.v3.service.ProxyPortService;
import com.example.lpm.v3.util.ExecuteCommandUtil;
import com.example.lpm.v3.util.RolaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Slf4j
@RequiredArgsConstructor
@Component
public class RolaProxyStrategy implements ProxyStrategy {

    private final RedissonClient redissonClient;
    private final ProxyIpService proxyIpService;

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;

    private final AccountInfoService accountInfoService;

    @Override
    public ProxyIpDO getProxyIp(LuaGetProxyIpQuery luaGetProxyIpQuery) {

        RLock rLock = redissonClient
            .getLock(RedisLockKeyConstant.LUA_GET_PROXY_IP_KEY + luaGetProxyIpQuery.getProxyIpType().getTypeName());
        if (rLock.isLocked()) {
            log.error("getProxyIp获取锁失败:{}", RedisLockKeyConstant.LUA_GET_PROXY_IP_KEY);
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
            if (CollUtil.isNotEmpty(luaGetProxyIpQuery.getZipCodeList())) {
                // 先distance排序 小到大
                // "zip_code"= "78717",”78665“ + risk <= middle risk + status 1 list
                // list取第一个
                // 更新 rolaIpDO.setStatus(4);
                // 返回
                List<LuaZipCodeQuery> zipCodeQueryList = luaGetProxyIpQuery.getZipCodeList();
                List<String> zipCodeList =
                    zipCodeQueryList.stream().sorted(Comparator.comparingDouble(LuaZipCodeQuery::getDistance))
                        .map(LuaZipCodeQuery::getZipCode).collect(Collectors.toList());

                List<ProxyIpDO> proxyIpDOList = proxyIpService.list(new QueryWrapper<ProxyIpDO>().lambda()
                    .eq(ProxyIpDO::getStatus, 1).in(ProxyIpDO::getPostalCode, zipCodeList).le(ProxyIpDO::getRisk, 80)
                    .eq(ProxyIpDO::getTypeName, luaGetProxyIpQuery.getProxyIpType())
                    .apply(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getFileType()),
                        "!json_contains(file_type, concat('\"" + luaGetProxyIpQuery.getFileType() + "\"'))"));

                if (CollUtil.isEmpty(proxyIpDOList)) {
                    throw new BizException(ReturnCode.RC999.getCode(), "没有符合条件的ROLA-IP");
                }

                ProxyIpDO proxyIpDO = proxyIpDOList.get(0);
                proxyIpDO.setStatus(4);
                proxyIpService.updateById(proxyIpDO);

                return proxyIpDO;

            }

            long count = proxyIpService.count(new QueryWrapper<ProxyIpDO>().lambda().eq(ProxyIpDO::getStatus, 1)
                .eq(ProxyIpDO::getTypeName, luaGetProxyIpQuery.getProxyIpType())
                .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getCountry()), ProxyIpDO::getCountry,
                    luaGetProxyIpQuery.getCountry())
                .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getState()), ProxyIpDO::getRegion,
                    luaGetProxyIpQuery.getState())
                .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getCity()), ProxyIpDO::getCity,
                    luaGetProxyIpQuery.getCity())
                .le(ProxyIpDO::getRisk, 80)
                .likeRight(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getIp()), ProxyIpDO::getIp,
                    luaGetProxyIpQuery.getIp())
                .likeRight(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getZipCode()), ProxyIpDO::getPostalCode,
                    luaGetProxyIpQuery.getZipCode())
                .apply(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getFileType()),
                    "!json_contains(file_type, concat('\"" + luaGetProxyIpQuery.getFileType() + "\"'))"));
            if (count > 0) {

                int c = RandomUtil.randomInt(0, (int)count);
                ProxyIpDO proxyIpDO = proxyIpService.getOne(new QueryWrapper<ProxyIpDO>().lambda()
                    .eq(ProxyIpDO::getStatus, 1).eq(ProxyIpDO::getTypeName, luaGetProxyIpQuery.getProxyIpType())
                    .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getCountry()), ProxyIpDO::getCountry,
                        luaGetProxyIpQuery.getCountry())
                    .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getState()), ProxyIpDO::getRegion,
                        luaGetProxyIpQuery.getState())
                    .eq(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getCity()), ProxyIpDO::getCity,
                        luaGetProxyIpQuery.getCity())
                    .le(ProxyIpDO::getRisk, 80)
                    .likeRight(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getIp()), ProxyIpDO::getIp,
                        luaGetProxyIpQuery.getIp())
                    .likeRight(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getZipCode()), ProxyIpDO::getPostalCode,
                        luaGetProxyIpQuery.getZipCode())
                    .apply(CharSequenceUtil.isNotBlank(luaGetProxyIpQuery.getFileType()),
                        "!json_contains(file_type, concat('\"" + luaGetProxyIpQuery.getFileType() + "\"'))")
                    .last("limit " + c + " , 1"));

                proxyIpDO.setStatus(4);
                proxyIpService.updateById(proxyIpDO);

                return proxyIpDO;
            } else {
                throw new BizException(ReturnCode.RC999.getCode(), "没有符合条件的ROLA-IP");
            }
        } finally {
            rLock.unlock();
        }
    }

    @Override
    public void checkIpSurvival(CheckIpSurvivalRequest checkIpSurvivalRequest) {
        // 获取account
        AccountInfoDO accountInfoDO = accountInfoService.getOne(new QueryWrapper<AccountInfoDO>().lambda()
            .eq(AccountInfoDO::getTypeName, checkIpSurvivalRequest.getProxyIpType().getTypeName())
            .eq(AccountInfoDO::getStatus, 1));
        if (accountInfoDO == null) {
            throw new BizException("AccountInfo不存在");
        }
        if (StrUtil.isBlank(checkIpSurvivalRequest.getUsername())) {
            checkIpSurvivalRequest.setUsername(RolaUtil.randomUsername());
        }

        if (StrUtil.isBlank(checkIpSurvivalRequest.getPassword())) {
            checkIpSurvivalRequest.setPassword(accountInfoDO.getPassword());

        }
        if (StrUtil.isBlank(checkIpSurvivalRequest.getServer())) {
            checkIpSurvivalRequest.setServer(accountInfoDO.getServer());

        }
        if (StrUtil.isBlank(checkIpSurvivalRequest.getServerPort())) {
            checkIpSurvivalRequest.setServerPort(accountInfoDO.getServerPort());

        }

        ProxyIpDO proxyIpDO = proxyIpService.getOne(
            new QueryWrapper<ProxyIpDO>().lambda().eq(ProxyIpDO::getTypeName, checkIpSurvivalRequest.getProxyIpType())
                .eq(ProxyIpDO::getIp, checkIpSurvivalRequest.getIp()));

        if (proxyIpDO == null) {
            throw new BizException(900, "IP不存在");
        }

        String rolaUsername = checkIpSurvivalRequest.getUsername() + "-ip-" + proxyIpDO.getIp();

        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(checkIpSurvivalRequest.getServer(),
            Integer.parseInt(checkIpSurvivalRequest.getServerPort())));
        java.net.Authenticator.setDefault(new java.net.Authenticator() {
            private final PasswordAuthentication authentication =
                new PasswordAuthentication(rolaUsername, checkIpSurvivalRequest.getPassword().toCharArray());

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return authentication;
            }
        });

        OkHttpClient client =
            new OkHttpClient().newBuilder().proxy(proxy).addInterceptor(new GzipRequestInterceptor()).build();
        Request request = new Request.Builder().url(ProxyConstant.LUMTEST_URL).build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            log.info(responseString);
            LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);

            if (!CharSequenceUtil.equals(proxyIpDO.getIp(), luminatiIPDTO.getIp())) {
                proxyIpDO.setStatus(0);
                proxyIpService.updateById(proxyIpDO);
                throw new BizException(901, "IP不相同");
            }

            Request request1 = new Request.Builder().url("https://www.paypal.com/signin").build();
            okhttp3.Response response1 = client.newCall(request1).execute();
            String responseString1 = response1.body().string();
            log.info("paypal http code:[]", response1.code());
            if (StrUtil.contains(responseString1, "403")) {
                proxyIpDO.setStatus(0);
                proxyIpService.updateById(proxyIpDO);
                throw new BizException(ReturnCode.RC500.getCode(), "调用paypal返回403");
            }
        } catch (Exception e) {
            proxyIpDO.setStatus(0);
            proxyIpService.updateById(proxyIpDO);
            throw new BizException(ReturnCode.RC500.getCode(), "调用lumtest返回失败");
        }
    }

    private final ProxyPortService proxyPortService;

    @Override
    public boolean startProxyPort(StartProxyPortRequest startProxyPortRequest) {

        long count = proxyPortService.count(
            new QueryWrapper<ProxyPortDO>().lambda().eq(ProxyPortDO::getProxyPort, startProxyPortRequest.getProxyPort())
                .eq(ProxyPortDO::getTypeName, startProxyPortRequest.getProxyIpType()));
        if (count > 0) {
            throw new BizException(997, "端口在使用中");
        }

        ProxyIpDO proxyIpDO = proxyIpService
            .getOne(new QueryWrapper<ProxyIpDO>().lambda().eq(ProxyIpDO::getIp, startProxyPortRequest.getProxyIp())
                .eq(ProxyIpDO::getTypeName, startProxyPortRequest.getProxyIpType()));
        if (proxyIpDO == null) {
            throw new BizException(ReturnCode.RC999.getCode(), "IP不存在");
        }

        AccountInfoDO accountInfoDO = accountInfoService.getOne(new QueryWrapper<AccountInfoDO>().lambda()
            .eq(AccountInfoDO::getTypeName, startProxyPortRequest.getProxyIpType().getTypeName())
            .eq(AccountInfoDO::getStatus, 1));
        if (accountInfoDO == null) {
            throw new BizException("AccountInfo不存在");
        }
        if (CharSequenceUtil.isBlank(startProxyPortRequest.getUsername())) {
            startProxyPortRequest.setUsername(RolaUtil.randomUsername());
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
        String rolaUsername = startProxyPortRequest.getUsername() + "-ip-" + startProxyPortRequest.getProxyIp();

        try {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(startProxyPortRequest.getServer(),
                Integer.parseInt(startProxyPortRequest.getServerPort())));
            java.net.Authenticator.setDefault(new java.net.Authenticator() {

                private final PasswordAuthentication authentication =
                    new PasswordAuthentication(rolaUsername, startProxyPortRequest.getPassword().toCharArray());

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return authentication;
                }
            });

            OkHttpClient client =
                new OkHttpClient().newBuilder().proxy(proxy).addInterceptor(new GzipRequestInterceptor()).build();
            Request request = new Request.Builder().url(ProxyConstant.LUMTEST_URL).build();

            okhttp3.Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);

            if (!CharSequenceUtil.equals(proxyIpDO.getIp(), luminatiIPDTO.getIp())) {
                proxyIpDO.setStatus(0);
                proxyIpService.updateById(proxyIpDO);
                throw new BizException(998, "IP不相等");
            }
        } catch (Exception e) {
            proxyIpDO.setStatus(0);
            proxyIpService.updateById(proxyIpDO);
            throw new BizException(ReturnCode.RC999.getCode(), "调用lumtest返回失败");
        }

        ExecuteCommandUtil.executeRolaProxySps(startProxyPortRequest.getProxyPort(),
            startProxyPortRequest.getProxyUsername(), startProxyPortRequest.getProxyPassword(),
            startProxyPortRequest.getProxyIp(), startProxyPortRequest.getUsername(),
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
        RBlockingQueue<CollectionTaskRequest> queue = redissonClient.getBlockingQueue(
            RedisKeyConstant.COLLECTION_TASK_TOPIC + collectionTaskRequest.getProxyIpType().getTypeName());

        if (queue.size() > 0) {
            throw new BizException(ReturnCode.RC999.getCode(),
                collectionTaskRequest.getProxyIpType().getTypeName() + "已有项目在执行，请等待完成后，再次增加收录项目。");
        }

        // 暂停，开始
        RAtomicLong collectFlag = redissonClient.getAtomicLong(
            RedisKeyConstant.COLLECTION_TASK_FLAG + collectionTaskRequest.getProxyIpType().getTypeName());
        // 开始 10
        collectFlag.set(10L);

        // 当前任务适量
        RAtomicLong currentNum = redissonClient.getAtomicLong(
            RedisKeyConstant.COLLECTION_TASK_CURRENT + collectionTaskRequest.getProxyIpType().getTypeName());
        currentNum.set(collectionTaskRequest.getNumber());

        // 放入队列
        for (int i = 0; i < collectionTaskRequest.getNumber(); i++) {
            queue.offer(collectionTaskRequest);
        }

        redisTemplate
            .delete(RedisKeyConstant.COLLECTION_TASK_ERROR + collectionTaskRequest.getProxyIpType().getTypeName());

    }

    @Override
    public void getCollectionProgress() {

    }

    @Override
    public ProxyIpType getStrategyName() {
        return ProxyIpType.ROLA;
    }
}
