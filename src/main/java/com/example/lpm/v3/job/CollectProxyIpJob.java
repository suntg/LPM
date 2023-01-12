//package com.example.lpm.v3.job;
//
//import java.net.InetSocketAddress;
//import java.net.PasswordAuthentication;
//import java.net.Proxy;
//
//import org.redisson.api.RAtomicLong;
//import org.redisson.api.RBlockingQueue;
//import org.redisson.api.RedissonClient;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import com.alibaba.fastjson2.JSON;
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.example.lpm.config.LuminatiProperties;
//import com.example.lpm.constant.ProxyConstant;
//import com.example.lpm.domain.dto.LuminatiIPDTO;
//import com.example.lpm.v3.common.BizException;
//import com.example.lpm.v3.common.ReturnCode;
//import com.example.lpm.v3.config.GzipRequestInterceptor;
//import com.example.lpm.v3.constant.ProxyIpType;
//import com.example.lpm.v3.constant.RedisKeyConstant;
//import com.example.lpm.v3.domain.dto.Ip123FraudDTO;
//import com.example.lpm.v3.domain.dto.Ip123InfoDTO;
//import com.example.lpm.v3.domain.entity.ProxyIpDO;
//import com.example.lpm.v3.domain.request.CollectionTaskRequest;
//import com.example.lpm.v3.service.ProxyIpService;
//import com.example.lpm.v3.util.RolaUtil;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.xxl.job.core.context.XxlJobHelper;
//import com.xxl.job.core.handler.annotation.XxlJob;
//
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.core.exceptions.ExceptionUtil;
//import cn.hutool.core.text.CharSequenceUtil;
//import cn.hutool.core.util.ObjectUtil;
//import cn.hutool.http.HttpRequest;
//import cn.hutool.http.HttpResponse;
//import cn.hutool.http.HttpStatus;
//import cn.hutool.http.HttpUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class CollectProxyIpJob {
//
//    private final RedissonClient redissonClient;
//
//    private final RedisTemplate<String, String> redisTemplate;
//
//    private final ObjectMapper objectMapper;
//
//    private final ProxyIpService proxyIpService;
//
//    @XxlJob("rolaCollectionJobHandler")
//    public void rolaCollectionJobHandler() {
//        XxlJobHelper.log("XXL-JOB, rolaCollectionJobHandler");
//        RAtomicLong collectFlag =
//            redissonClient.getAtomicLong(RedisKeyConstant.COLLECTION_TASK_FLAG + ProxyIpType.ROLA.getTypeName());
//        // log.info(" rolaCollectionJobHandler collectFlag : {}", collectFlag);
//        if (collectFlag.get() == 10L) {
//            RBlockingQueue<CollectionTaskRequest> queue = redissonClient
//                .getBlockingQueue(RedisKeyConstant.COLLECTION_TASK_TOPIC + ProxyIpType.ROLA.getTypeName());
//            CollectionTaskRequest collectionTaskRequest = queue.poll();
//            if (ObjectUtil.isNotNull(collectionTaskRequest)) {
//                String user = RolaUtil.randomUsername();
//                try {
//                    String result = RolaUtil.refresh(user, collectionTaskRequest.getCountry(),
//                        collectionTaskRequest.getState(), collectionTaskRequest.getCity());
//                    if (CharSequenceUtil.contains(result, "SUCCESS")) {
//
//                        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("gate2.rola.info", 2042));
//                        java.net.Authenticator.setDefault(new java.net.Authenticator() {
//                            private final PasswordAuthentication authentication =
//                                new PasswordAuthentication(user, "209209us".toCharArray());
//
//                            @Override
//                            protected PasswordAuthentication getPasswordAuthentication() {
//                                return authentication;
//                            }
//                        });
//                        OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy)
//                            .addInterceptor(new GzipRequestInterceptor()).build();
//                        Request request = new Request.Builder().url(ProxyConstant.LUMTEST_URL).build();
//                        okhttp3.Response response = client.newCall(request).execute();
//                        String responseString = response.body().string();
//                        log.info("rola lumtest :{}", responseString);
//                        if (responseString.contains("No peer available")) {
//                            collectFlag.set(12L);
//                            queue.clear();
//                            redisTemplate
//                                .boundValueOps(RedisKeyConstant.COLLECTION_TASK_ERROR + ProxyIpType.ROLA.getTypeName())
//                                .set("No peer available");
//                        } else {
//                            LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);
//                            // 如果城市 或者 州 为nul 调用http://ip123.in/search_ip?ip=xxx 补齐
//
//                            if (CharSequenceUtil.hasBlank(luminatiIPDTO.getGeo().getRegion(),
//                                luminatiIPDTO.getGeo().getCity())) {
//                                String ip123InfoResult =
//                                    HttpUtil.get("http://ip123.in/search_ip?ip=" + luminatiIPDTO.getIp());
//
//                                JsonNode jsonNode = objectMapper.readTree(ip123InfoResult);
//
//                                Ip123InfoDTO ip123InfoDTO =
//                                    objectMapper.readValue(jsonNode.get("data").toString(), Ip123InfoDTO.class);
//
//                                luminatiIPDTO.getGeo().setRegion(ip123InfoDTO.getRegionCode());
//                                luminatiIPDTO.getGeo().setCity(ip123InfoDTO.getCity());
//                                luminatiIPDTO.getGeo().setPostalCode(ip123InfoDTO.getPostal());
//                            }
//                            if (CharSequenceUtil.hasBlank(luminatiIPDTO.getGeo().getRegion(),
//                                luminatiIPDTO.getGeo().getCity())) {
//                                throw new BizException(ReturnCode.RC999.getCode(), ReturnCode.RC999.getMessage());
//                            }
//                            String ip123FraudResult =
//                                HttpUtil.get("http://www.ip123.in/fraud_check?ip=" + luminatiIPDTO.getIp());
//                            JsonNode jsonNode = objectMapper.readTree(ip123FraudResult);
//                            Ip123FraudDTO ip123FraudDTO =
//                                objectMapper.readValue(jsonNode.get("data").toString(), Ip123FraudDTO.class);
//                            save(luminatiIPDTO, ip123FraudDTO, ProxyIpType.ROLA, null);
//
//                            RAtomicLong totalNum = redissonClient
//                                .getAtomicLong(RedisKeyConstant.COLLECTION_TASK_TOTAL + ProxyIpType.ROLA.getTypeName());
//                            totalNum.incrementAndGet();
//
//                            String today = DateUtil.today();
//                            RAtomicLong todayNum = redissonClient.getAtomicLong(
//                                RedisKeyConstant.COLLECTION_TASK_TODAY + ProxyIpType.ROLA.getTypeName() + today);
//                            todayNum.incrementAndGet();
//                        }
//                    } else {
//                        RAtomicLong currentRepeatNum = redissonClient
//                            .getAtomicLong(RedisKeyConstant.COLLECTION_TASK_FAIL + ProxyIpType.ROLA.getTypeName());
//                        currentRepeatNum.incrementAndGet();
//                    }
//                } catch (Exception e) {
//                    RAtomicLong currentRepeatNum = redissonClient
//                        .getAtomicLong(RedisKeyConstant.COLLECTION_TASK_FAIL + ProxyIpType.ROLA.getTypeName());
//                    currentRepeatNum.incrementAndGet();
//                    log.error("rolaCollectionJobHandler Exception: {}", ExceptionUtil.stacktraceToString(e));
//                }
//
//            }
//        } else {
//            // log.info("rolaCollectionJobHandler 不运行");
//        }
//    }
//
//    private final LuminatiProperties luminatiProperties;
//
//    @XxlJob("luminatiCollectionJobHandler")
//    public void luminatiCollectionJobHandler() throws Exception {
//        XxlJobHelper.log("XXL-JOB, luminatiCollectionJobHandler");
//        RAtomicLong collectFlag =
//            redissonClient.getAtomicLong(RedisKeyConstant.COLLECTION_TASK_FLAG + ProxyIpType.LUMINATI.getTypeName());
//        //log.info(" rolaCollectionJobHandler collectFlag : {}", collectFlag);
//        if (collectFlag.get() == 10L) {
//            RBlockingQueue<CollectionTaskRequest> queue = redissonClient
//                .getBlockingQueue(RedisKeyConstant.COLLECTION_TASK_TOPIC + ProxyIpType.LUMINATI.getTypeName());
//            CollectionTaskRequest collectionTaskRequest = queue.poll();
//            if (ObjectUtil.isNotNull(collectionTaskRequest)) {
//                String url = "http://lumtest.com/myip.json";
//                try {
//                    String country = collectionTaskRequest.getCountry();
//                    String state = collectionTaskRequest.getState();
//                    String city = collectionTaskRequest.getCity();
//                    HttpResponse response;
//                    if (CharSequenceUtil.isAllNotBlank(country, state, city)) {
//                        String proxyUsername = CharSequenceUtil.format(
//                            "lum-customer-c_99c3c376-zone-zone_city_ly-country-{}-state-{}-city-{}",
//                            CharSequenceUtil.cleanBlank(country).toLowerCase(),
//                            CharSequenceUtil.cleanBlank(state).toLowerCase(),
//                            CharSequenceUtil.cleanBlank(city).toLowerCase());
//                        response = HttpRequest.get(url)
//                            .setProxy(new Proxy(Proxy.Type.HTTP,
//                                new InetSocketAddress(luminatiProperties.getProxyHost(),
//                                    luminatiProperties.getProxyPort())))
//                            .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
//                    } else if (CharSequenceUtil.isBlank(country) && CharSequenceUtil.isAllNotBlank(state, city)) {
//                        String proxyUsername = CharSequenceUtil.format(
//                            "lum-customer-c_99c3c376-zone-zone_city_ly-country-us-state-{}-city-{}",
//                            CharSequenceUtil.cleanBlank(state).toLowerCase(),
//                            CharSequenceUtil.cleanBlank(city).toLowerCase());
//                        response = HttpRequest.get(url)
//                            .setProxy(new Proxy(Proxy.Type.HTTP,
//                                new InetSocketAddress(luminatiProperties.getProxyHost(),
//                                    luminatiProperties.getProxyPort())))
//                            .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
//                    } else if (CharSequenceUtil.isNotBlank(country) && CharSequenceUtil.isNotBlank(state)
//                        && CharSequenceUtil.isBlank(city)) {
//                        String proxyUsername =
//                            CharSequenceUtil.format("lum-customer-c_99c3c376-zone-zone_city_ly-country-{}-state-{}",
//                                CharSequenceUtil.cleanBlank(country).toLowerCase(),
//                                CharSequenceUtil.cleanBlank(state).toLowerCase());
//                        response = HttpRequest.get(url)
//                            .setProxy(new Proxy(Proxy.Type.HTTP,
//                                new InetSocketAddress(luminatiProperties.getProxyHost(),
//                                    luminatiProperties.getProxyPort())))
//                            .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
//                    } else if (CharSequenceUtil.isBlank(country) && CharSequenceUtil.isNotBlank(state)
//                        && CharSequenceUtil.isBlank(city)) {
//                        String proxyUsername =
//                            CharSequenceUtil.format("lum-customer-c_99c3c376-zone-zone_city_ly-country-us-state-{}",
//                                CharSequenceUtil.cleanBlank(state).toLowerCase());
//                        response = HttpRequest.get(url)
//                            .setProxy(new Proxy(Proxy.Type.HTTP,
//                                new InetSocketAddress(luminatiProperties.getProxyHost(),
//                                    luminatiProperties.getProxyPort())))
//                            .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
//                    } else {
//                        response = HttpRequest.get(url)
//                            .setProxy(new Proxy(Proxy.Type.HTTP,
//                                new InetSocketAddress(luminatiProperties.getProxyHost(),
//                                    luminatiProperties.getProxyPort())))
//                            .basicProxyAuth(luminatiProperties.getProxyUsername(),
//                                luminatiProperties.getProxyPassword())
//                            .setReadTimeout(5000).execute();
//                    }
//
//                    log.info("luminatiCollectionJobHandler lumtest result : {}", response.toString());
//
//                    if (HttpStatus.HTTP_OK == response.getStatus()) {
//                        String xLuminatiIP = response.header("x-luminati-ip");
//
//                        String luminatiResult = response.body();
//                        log.info("luminatiCollectionJobHandler lumtest response body : {}", luminatiResult);
//
//                        if (luminatiResult.contains("No peer available")) {
//                            collectFlag.set(12L);
//                            queue.clear();
//                            redisTemplate
//                                .boundValueOps(
//                                    RedisKeyConstant.COLLECTION_TASK_ERROR + ProxyIpType.LUMINATI.getTypeName())
//                                .set("No peer available");
//                        } else {
//                            LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(luminatiResult, LuminatiIPDTO.class);
//                            if (CharSequenceUtil.isAllNotEmpty(luminatiIPDTO.getCountry(),
//                                luminatiIPDTO.getGeo().getRegion(), luminatiIPDTO.getGeo().getCity())) {
//                                log.info("luminatiIPDTO :{}", JSON.toJSONString(luminatiIPDTO));
//                                this.save(luminatiIPDTO, null, ProxyIpType.LUMINATI, xLuminatiIP);
//
//                                RAtomicLong totalNum = redissonClient.getAtomicLong(
//                                    RedisKeyConstant.COLLECTION_TASK_TOTAL + ProxyIpType.LUMINATI.getTypeName());
//                                totalNum.incrementAndGet();
//
//                                String today = DateUtil.today();
//                                RAtomicLong todayNum =
//                                    redissonClient.getAtomicLong(RedisKeyConstant.COLLECTION_TASK_TODAY
//                                        + ProxyIpType.LUMINATI.getTypeName() + today);
//                                todayNum.incrementAndGet();
//                            }
//
//                        }
//                    } else {
//                        RAtomicLong currentRepeatNum = redissonClient
//                            .getAtomicLong(RedisKeyConstant.COLLECTION_TASK_FAIL + ProxyIpType.LUMINATI.getTypeName());
//                        currentRepeatNum.incrementAndGet();
//                    }
//                } catch (Exception e) {
//                    RAtomicLong currentRepeatNum = redissonClient
//                        .getAtomicLong(RedisKeyConstant.COLLECTION_TASK_FAIL + ProxyIpType.LUMINATI.getTypeName());
//                    currentRepeatNum.incrementAndGet();
//                    log.error("luminatiCollectionJobHandler Exception: {}", ExceptionUtil.stacktraceToString(e));
//                }
//            }
//
//        } else {
//            //log.info("luminatiCollectionJobHandler 不运行");
//        }
//    }
//
//    private void save(LuminatiIPDTO luminatiIPDTO, Ip123FraudDTO ip123FraudDTO, ProxyIpType proxyIpType,
//        String xLuminatiIP) {
//
//        long count = proxyIpService.count(new QueryWrapper<ProxyIpDO>().lambda()
//            .eq(ProxyIpDO::getIp, luminatiIPDTO.getIp()).eq(ProxyIpDO::getTypeName, proxyIpType.getTypeName()));
//        if (count > 0) {
//            log.info("已存在IP: {} , Type: {}", luminatiIPDTO.getIp(), proxyIpType.getTypeName());
//
//            RAtomicLong currentRepeatNum =
//                redissonClient.getAtomicLong(RedisKeyConstant.COLLECTION_TASK_REPEAT + proxyIpType.getTypeName());
//            currentRepeatNum.incrementAndGet();
//        } else {
//            ProxyIpDO proxyIpDO = new ProxyIpDO();
//            proxyIpDO.setIp(luminatiIPDTO.getIp());
//            proxyIpDO.setCountry(luminatiIPDTO.getCountry().toLowerCase());
//            proxyIpDO.setRegion(luminatiIPDTO.getGeo().getRegion().toLowerCase());
//            proxyIpDO.setCity(luminatiIPDTO.getGeo().getCity().toLowerCase());
//            proxyIpDO.setPostalCode(luminatiIPDTO.getGeo().getPostalCode());
//            proxyIpDO.setTimezone(luminatiIPDTO.getGeo().getTz());
//            proxyIpDO.setTypeName(proxyIpType);
//
//            if (ObjectUtil.isNotNull(ip123FraudDTO)) {
//                proxyIpDO.setRisk(ip123FraudDTO.getRisk());
//                proxyIpDO.setRiskEnglish(ip123FraudDTO.getRiskEnglish());
//                proxyIpDO.setScore(ip123FraudDTO.getScore());
//            }
//            if (CharSequenceUtil.isNotBlank(xLuminatiIP)) {
//                proxyIpDO.setXLuminatiIp(xLuminatiIP);
//            }
//            proxyIpService.save(proxyIpDO);
//
//            log.info("插入新数据: {}", proxyIpDO);
//        }
//
//    }
//}
