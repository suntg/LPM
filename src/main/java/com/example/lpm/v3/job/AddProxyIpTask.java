package com.example.lpm.v3.job;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.config.LuminatiProperties;
import com.example.lpm.constant.ProxyConstant;
import com.example.lpm.domain.dto.LuminatiIPDTO;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.v3.common.ReturnCode;
import com.example.lpm.v3.config.GzipRequestInterceptor;
import com.example.lpm.v3.constant.ProxyIpType;
import com.example.lpm.v3.constant.RedisKeyConstant;
import com.example.lpm.v3.domain.dto.Ip123FraudDTO;
import com.example.lpm.v3.domain.dto.Ip123InfoDTO;
import com.example.lpm.v3.domain.entity.AccountInfoDO;
import com.example.lpm.v3.domain.entity.ProxyIpDO;
import com.example.lpm.v3.domain.request.CollectionTaskRequest;
import com.example.lpm.v3.service.AccountInfoService;
import com.example.lpm.v3.service.ProxyIpService;
import com.example.lpm.v3.util.RolaUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Slf4j
@Configuration
@EnableScheduling
@EnableAsync
@RequiredArgsConstructor
public class AddProxyIpTask {

    private final RedissonClient redissonClient;

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;

    private final ProxyIpService proxyIpService;

    private final LuminatiProperties luminatiProperties;

    private final AccountInfoService accountInfoService;

    @Scheduled(fixedDelay = 500)
    @Async("rolaAddProxyIpTaskExecutor")
    public void rolaCollectionJobHandler() {
        RBlockingQueue<CollectionTaskRequest> queue =
            redissonClient.getBlockingQueue(RedisKeyConstant.ADD_PROXY_IP_TASK_TOPIC + ProxyIpType.ROLA.getTypeName());
        CollectionTaskRequest collectionTaskRequest = queue.poll();
        if (ObjectUtil.isNotNull(collectionTaskRequest)) {
            String user = RolaUtil.randomUsername();
            try {
                AccountInfoDO accountInfoDO = accountInfoService.getOne(new QueryWrapper<AccountInfoDO>().lambda()
                    .eq(AccountInfoDO::getTypeName, ProxyIpType.ROLA.getTypeName()).eq(AccountInfoDO::getStatus, 1));
                if (ObjectUtil.isNull(accountInfoDO)) {
                    return;
                }
                String result =
                    RolaUtil.refresh(accountInfoDO.getRefreshServer(), user, collectionTaskRequest.getCountry(),
                        collectionTaskRequest.getState(), collectionTaskRequest.getCity());
                if (CharSequenceUtil.contains(result, "SUCCESS")) {

                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(accountInfoDO.getServer(),
                        Integer.parseInt(accountInfoDO.getServerPort())));
                    java.net.Authenticator.setDefault(new java.net.Authenticator() {
                        private final PasswordAuthentication authentication =
                            new PasswordAuthentication(user, accountInfoDO.getPassword().toCharArray());

                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return authentication;
                        }
                    });
                    OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy)
                        .addInterceptor(new GzipRequestInterceptor()).build();
                    Request request = new Request.Builder().url(ProxyConstant.LUMTEST_URL).build();
                    okhttp3.Response response = client.newCall(request).execute();
                    String responseString = response.body().string();
                    log.info("rola Add Proxy IP result :{}", responseString);
                    if (responseString.contains("No peer available")) {
                        queue.clear();
                        http: // lumtest.com/myip.json.clear();
                        redisTemplate
                            .boundValueOps(RedisKeyConstant.COLLECTION_TASK_ERROR + ProxyIpType.ROLA.getTypeName())
                            .set("No peer available");
                    } else {
                        LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);
                        // 如果城市 或者 州 为nul 调用http://ip123.in/search_ip?ip=xxx 补齐

                        if (CharSequenceUtil.hasBlank(luminatiIPDTO.getGeo().getRegion(),
                            luminatiIPDTO.getGeo().getCity())) {
                            String ip123InfoResult =
                                HttpUtil.get("http://ip123.in/search_ip?ip=" + luminatiIPDTO.getIp());

                            JsonNode jsonNode = objectMapper.readTree(ip123InfoResult);

                            Ip123InfoDTO ip123InfoDTO =
                                objectMapper.readValue(jsonNode.get("data").toString(), Ip123InfoDTO.class);

                            luminatiIPDTO.getGeo().setRegion(ip123InfoDTO.getRegionCode());
                            luminatiIPDTO.getGeo().setCity(ip123InfoDTO.getCity());
                            luminatiIPDTO.getGeo().setPostalCode(ip123InfoDTO.getPostal());
                        }
                        if (CharSequenceUtil.hasBlank(luminatiIPDTO.getGeo().getRegion(),
                            luminatiIPDTO.getGeo().getCity())) {
                            throw new BizException(ReturnCode.RC999.getCode(), ReturnCode.RC999.getMessage());
                        }
                        String ip123FraudResult =
                            HttpUtil.get("http://www.ip123.in/fraud_check?ip=" + luminatiIPDTO.getIp());
                        JsonNode jsonNode = objectMapper.readTree(ip123FraudResult);
                        Ip123FraudDTO ip123FraudDTO =
                            objectMapper.readValue(jsonNode.get("data").toString(), Ip123FraudDTO.class);
                        save(luminatiIPDTO, ip123FraudDTO, ProxyIpType.ROLA, null);

                        RAtomicLong totalNum = redissonClient
                            .getAtomicLong(RedisKeyConstant.COLLECTION_TASK_TOTAL + ProxyIpType.ROLA.getTypeName());
                        totalNum.incrementAndGet();

                        String today = DateUtil.today();
                        RAtomicLong todayNum = redissonClient.getAtomicLong(
                            RedisKeyConstant.COLLECTION_TASK_TODAY + ProxyIpType.ROLA.getTypeName() + today);
                        todayNum.incrementAndGet();
                    }
                } else {
                    RAtomicLong currentRepeatNum = redissonClient
                        .getAtomicLong(RedisKeyConstant.COLLECTION_TASK_FAIL + ProxyIpType.ROLA.getTypeName());
                    currentRepeatNum.incrementAndGet();
                }
            } catch (Exception e) {
                RAtomicLong currentRepeatNum = redissonClient
                    .getAtomicLong(RedisKeyConstant.COLLECTION_TASK_FAIL + ProxyIpType.ROLA.getTypeName());
                currentRepeatNum.incrementAndGet();
                log.error("rolaAddIPJobHandler Exception: {}", ExceptionUtil.stacktraceToString(e));
            }

        }
    }

    @Scheduled(fixedDelay = 500)
    @Async("luminatiAddProxyIpTaskExecutor")
    public void luminatiCollectionJobHandler() {
        RBlockingQueue<CollectionTaskRequest> queue = redissonClient
            .getBlockingQueue(RedisKeyConstant.ADD_PROXY_IP_TASK_TOPIC + ProxyIpType.LUMINATI.getTypeName());
        CollectionTaskRequest collectionTaskRequest = queue.poll();
        if (ObjectUtil.isNotNull(collectionTaskRequest)) {
            String url = "http://lumtest.com/myip.json";
            try {
                String country = collectionTaskRequest.getCountry();
                String state = collectionTaskRequest.getState();
                String city = collectionTaskRequest.getCity();
                HttpResponse response;

                if (CharSequenceUtil.isNotBlank(country)) {
                    AccountInfoDO accountInfoDO = accountInfoService.getOne(new QueryWrapper<AccountInfoDO>().lambda()
                        .eq(AccountInfoDO::getTypeName, ProxyIpType.LUMINATI.getTypeName())
                        .eq(AccountInfoDO::getZone, "zzz_d10").eq(AccountInfoDO::getStatus, 1));
                    if (ObjectUtil.isNull(accountInfoDO)) {
                        return;
                    }
                    String proxyUsername = CharSequenceUtil.format(
                        "brd-customer-" + accountInfoDO.getUsername() + "-zone-zzz_d10-country-{}",
                        CharSequenceUtil.cleanBlank(country).toLowerCase());
                    response = HttpRequest.get(url)
                        .setProxy(new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress(luminatiProperties.getProxyHost(),
                                luminatiProperties.getProxyPort())))
                        .basicProxyAuth(proxyUsername, accountInfoDO.getPassword()).setReadTimeout(5000).execute();
                } else if (CharSequenceUtil.isAllNotBlank(state, city)) {
                    AccountInfoDO accountInfoDO = accountInfoService.getOne(new QueryWrapper<AccountInfoDO>().lambda()
                        .eq(AccountInfoDO::getTypeName, ProxyIpType.LUMINATI.getTypeName())
                        .eq(AccountInfoDO::getZone, "zzz_d9").eq(AccountInfoDO::getStatus, 1));
                    if (ObjectUtil.isNull(accountInfoDO)) {
                        return;
                    }
                    String proxyUsername = CharSequenceUtil.format(
                        "brd-customer-" + accountInfoDO.getUsername() + "-zone-zzz_d9-country-us-state-{}-city-{}",
                        CharSequenceUtil.cleanBlank(state).toLowerCase(),
                        CharSequenceUtil.cleanBlank(city).toLowerCase());
                    response = HttpRequest.get(url)
                        .setProxy(new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress(luminatiProperties.getProxyHost(),
                                luminatiProperties.getProxyPort())))
                        .basicProxyAuth(proxyUsername, accountInfoDO.getPassword()).setReadTimeout(5000).execute();
                } else {
                    /*response = HttpRequest.get(url)
                        .setProxy(new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress(luminatiProperties.getProxyHost(),
                                luminatiProperties.getProxyPort())))
                        .basicProxyAuth(luminatiProperties.getProxyUsername(), luminatiProperties.getProxyPassword())
                        .setReadTimeout(5000).execute();*/
                    return;
                }

                log.info("luminatiAddProxyIPJobHandler lumtest result : {}", response.toString());

                if (HttpStatus.HTTP_OK == response.getStatus()) {
                    String xLuminatiIP = response.header("x-luminati-ip");

                    String luminatiResult = response.body();
                    log.info("luminatiAddProxyIpJobHandler lumtest response body : {}", luminatiResult);

                    if (luminatiResult.contains("No peer available")) {
                        queue.clear();
                        redisTemplate
                            .boundValueOps(RedisKeyConstant.COLLECTION_TASK_ERROR + ProxyIpType.LUMINATI.getTypeName())
                            .set("No peer available");
                    } else {
                        LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(luminatiResult, LuminatiIPDTO.class);
                        if (CharSequenceUtil.isAllNotEmpty(luminatiIPDTO.getCountry(),
                            luminatiIPDTO.getGeo().getRegion(), luminatiIPDTO.getGeo().getCity())) {
                            log.info("luminatiIPDTO :{}", JSON.toJSONString(luminatiIPDTO));

                            String ip123FraudResult =
                                HttpUtil.get("http://www.ip123.in/fraud_check?ip=" + luminatiIPDTO.getIp());
                            JsonNode jsonNode = objectMapper.readTree(ip123FraudResult);
                            Ip123FraudDTO ip123FraudDTO =
                                objectMapper.readValue(jsonNode.get("data").toString(), Ip123FraudDTO.class);

                            this.save(luminatiIPDTO, ip123FraudDTO, ProxyIpType.LUMINATI, xLuminatiIP);

                            RAtomicLong totalNum = redissonClient.getAtomicLong(
                                RedisKeyConstant.COLLECTION_TASK_TOTAL + ProxyIpType.LUMINATI.getTypeName());
                            totalNum.incrementAndGet();

                            String today = DateUtil.today();
                            RAtomicLong todayNum = redissonClient.getAtomicLong(
                                RedisKeyConstant.COLLECTION_TASK_TODAY + ProxyIpType.LUMINATI.getTypeName() + today);
                            todayNum.incrementAndGet();
                        }

                    }
                } else {
                    RAtomicLong currentRepeatNum = redissonClient
                        .getAtomicLong(RedisKeyConstant.COLLECTION_TASK_FAIL + ProxyIpType.LUMINATI.getTypeName());
                    currentRepeatNum.incrementAndGet();
                }
            } catch (Exception e) {
                RAtomicLong currentRepeatNum = redissonClient
                    .getAtomicLong(RedisKeyConstant.COLLECTION_TASK_FAIL + ProxyIpType.LUMINATI.getTypeName());
                currentRepeatNum.incrementAndGet();
                log.error("luminatiAddProxyIpJobHandler Exception: {}", ExceptionUtil.stacktraceToString(e));
            }
        }

    }

    private void save(LuminatiIPDTO luminatiIPDTO, Ip123FraudDTO ip123FraudDTO, ProxyIpType proxyIpType,
        String xLuminatiIP) {

        long count = proxyIpService.count(new QueryWrapper<ProxyIpDO>().lambda()
            .eq(ProxyIpDO::getIp, luminatiIPDTO.getIp()).eq(ProxyIpDO::getTypeName, proxyIpType.getTypeName()));
        if (count > 0) {
            log.info("已存在IP: {} , Type: {}", luminatiIPDTO.getIp(), proxyIpType.getTypeName());

            RAtomicLong currentRepeatNum =
                redissonClient.getAtomicLong(RedisKeyConstant.COLLECTION_TASK_REPEAT + proxyIpType.getTypeName());
            currentRepeatNum.incrementAndGet();
        } else {
            ProxyIpDO proxyIpDO = new ProxyIpDO();
            proxyIpDO.setIp(luminatiIPDTO.getIp());
            proxyIpDO.setCountry(luminatiIPDTO.getCountry().toLowerCase());
            proxyIpDO.setRegion(luminatiIPDTO.getGeo().getRegion().toLowerCase());
            proxyIpDO.setCity(luminatiIPDTO.getGeo().getCity().toLowerCase());
            proxyIpDO.setPostalCode(luminatiIPDTO.getGeo().getPostalCode());
            proxyIpDO.setTimezone(luminatiIPDTO.getGeo().getTz());
            proxyIpDO.setTypeName(proxyIpType);

            if (ObjectUtil.isNotNull(ip123FraudDTO)) {
                proxyIpDO.setRisk(ip123FraudDTO.getRisk());
                proxyIpDO.setRiskEnglish(ip123FraudDTO.getRiskEnglish());
                proxyIpDO.setScore(ip123FraudDTO.getScore());
            }
            if (CharSequenceUtil.isNotBlank(xLuminatiIP)) {
                proxyIpDO.setXLuminatiIp(xLuminatiIP);
            }

            proxyIpService.save(proxyIpDO);

            log.info("插入新数据: {}", proxyIpDO);
        }

    }
}
