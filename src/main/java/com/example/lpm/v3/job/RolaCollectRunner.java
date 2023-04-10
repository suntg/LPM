package com.example.lpm.v3.job;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.constant.ProxyConstant;
import com.example.lpm.constant.RedisKeyConstant;
import com.example.lpm.domain.dto.LuminatiIPDTO;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.v3.common.ReturnCode;
import com.example.lpm.v3.config.AsyncConfig;
import com.example.lpm.v3.config.GzipRequestInterceptor;
import com.example.lpm.v3.constant.RolaCollectConstant;
import com.example.lpm.v3.domain.dto.Ip123FraudDTO;
import com.example.lpm.v3.domain.dto.Ip123InfoDTO;
import com.example.lpm.v3.domain.dto.RolaCollectQueueMsgDTO;
import com.example.lpm.v3.domain.dto.RolaCollectResultDTO;
import com.example.lpm.v3.domain.entity.RolaIpDO;
import com.example.lpm.v3.domain.request.RolaIpRequest;
import com.example.lpm.v3.mapper.RolaIpMapper;
import com.example.lpm.v3.util.ExecuteCommandUtil;
import com.example.lpm.v3.util.RolaUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class RolaCollectRunner implements CommandLineRunner {

    private final RedissonClient redissonClient;

    private final ObjectMapper objectMapper;

    private final RolaIpMapper rolaIpMapper;

    private final AsyncConfig asyncConfig;

    private final RedisTemplate<String, Object> redisTemplate;

    static final String cnAddress = "gate2.rola.info";
    static final Integer cnPort = 2042;

    static final String usAddress = "proxyus.rola.info";
    static final Integer usPort = 2000;

    @Override
    public void run(String... args) throws Exception {
        /*for (int i = 0; i < 50; i++) {
            asyncConfig.collectRolaThreadPool().submit(this::collectV2);
            Thread.sleep(2000);
        }*/
        for (int i = 0; i < 4; i++) {
            asyncConfig.phoneCollectRolaThreadPool().submit(this::phoneCollect);
            Thread.sleep(2000);
        }
    }

    public void phoneCollect() {
        RBlockingQueue<RolaIpRequest> queue =
                redissonClient.getBlockingQueue(RedisKeyConstant.ROLA_PHONE_COLLECT_IP_QUEUE_KEY);
        while (true) {
            RolaIpRequest rolaIpRequest = null;
            try {
                rolaIpRequest = queue.take();

                String user = RolaUtil.randomUsername();
                String result = ExecuteCommandUtil.rolaRefresh(user, rolaIpRequest.getCountry(),
                        rolaIpRequest.getState(), rolaIpRequest.getCity());

                log.info("rola refresh :{}", result);

                if (CharSequenceUtil.contains(result, "SUCCESS")) {
                    Thread.sleep(2000);

                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(usAddress, usPort));
                    /*java.net.Authenticator.setDefault(new java.net.Authenticator() {
                        private PasswordAuthentication authentication =
                            new PasswordAuthentication(user, "209209us".toCharArray());
                    
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return authentication;
                        }
                    });*/
                    OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy)
                            .addInterceptor(new GzipRequestInterceptor()).authenticator(new Authenticator() {
                                final String credential = Credentials.basic(user, "Su902902");

                                @Nullable
                                @Override
                                public Request authenticate(@Nullable Route route, @NotNull Response response) {
                                    return response.request().newBuilder().header("Authorization", credential).build();
                                }
                            }).build();
                    Request request = new Request.Builder().url(ProxyConstant.LUMTEST_URL).build();

                    okhttp3.Response response = client.newCall(request).execute();

                    String responseString = response.body().string();

                    log.info("lumtest :{}", responseString);

                    LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);

                    // 如果城市 或者 州 为nul 调用http://ip123.in/search_ip?ip=xxx 补齐

                    if (CharSequenceUtil.hasBlank(luminatiIPDTO.getGeo().getRegion(),
                            luminatiIPDTO.getGeo().getCity())) {
                        String ip123InfoResult = HttpUtil.get("http://ip123.in/search_ip?ip=" + luminatiIPDTO.getIp());

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

                    save(luminatiIPDTO, ip123FraudDTO, usAddress);
                }
            } catch (Exception e) {
                log.error(" ROLA_PHONE_COLLECT_IP_QUEUE_KEY Exception:{}", ExceptionUtil.stacktraceToString(e));
            }

        }
    }

    public void collect() {
        RBlockingQueue<RolaIpRequest> queue =
                redissonClient.getBlockingQueue(RedisKeyConstant.ROLA_COLLECT_IP_QUEUE_KEY);
        while (true) {
            RolaIpRequest rolaIpRequest = null;
            try {
                RAtomicLong collectFlag = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_COLLECT_FLAG_KEY);
                if (collectFlag.get() == 11L) {
                    // 11 停止
                    Thread.sleep(3000);
                    continue;
                }
                // 10
                rolaIpRequest = queue.take();

                String user = RolaUtil.randomUsername();
                String result = ExecuteCommandUtil.rolaRefresh(user, rolaIpRequest.getCountry(),
                        rolaIpRequest.getState(), rolaIpRequest.getCity());

                log.info("rola refresh :{}", result);

                if (CharSequenceUtil.contains(result, "SUCCESS")) {
                    Thread.sleep(1500);

                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(usAddress, usPort));
                    java.net.Authenticator.setDefault(new java.net.Authenticator() {
                        private final PasswordAuthentication authentication =
                                new PasswordAuthentication(user, "Su902902".toCharArray());

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

                    log.info("lumtest :{}", responseString);
                    // if (responseString.contains("No peer available")) {
                    //     collectFlag.set(12L);
                    //     queue.clear();
                    //     redisTemplate.boundValueOps(RedisKeyConstant.ROLA_COLLECT_ERROR_KEY).set("No peer available");
                    //     continue;
                    // }

                    LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);

                    // 如果城市 或者 州 为nul 调用http://ip123.in/search_ip?ip=xxx 补齐

                    if (CharSequenceUtil.hasBlank(luminatiIPDTO.getGeo().getRegion(),
                            luminatiIPDTO.getGeo().getCity())) {
                        String ip123InfoResult = HttpUtil.get("http://ip123.in/search_ip?ip=" + luminatiIPDTO.getIp());

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

                    save(luminatiIPDTO, ip123FraudDTO, usAddress);

                    RAtomicLong totalNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_TOTAL_KEY);
                    totalNum.incrementAndGet();

                    String today = DateUtil.today();
                    RAtomicLong todayNum = redissonClient.getAtomicLong("#ROLA_" + today);
                    todayNum.incrementAndGet();

                }
            } catch (Exception e) {
                RAtomicLong currentFailNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_CURRENT_FAIL_KEY);
                currentFailNum.incrementAndGet();

                log.error(" ROLA_COLLECT_IP_QUEUE Exception:{}", ExceptionUtil.stacktraceToString(e));
            }

        }
    }


    public void collectV2() {
        RBlockingQueue<String> queue =
                redissonClient.getBlockingQueue(RedisKeyConstant.ROLA_COLLECT_IP_QUEUE_KEY_V2);
        while (true) {
            String rolaProxy = null;
            try {
                RAtomicLong collectFlag = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_COLLECT_FLAG_KEY);
                if (collectFlag.get() == 11L) {
                    // 11 停止
                    Thread.sleep(3000);
                    continue;
                }
                // 10
                rolaProxy = queue.take();

                // String user = RolaUtil.randomUsername();
                // String result = ExecuteCommandUtil.rolaRefresh(user, rolaIpRequest.getCountry(),
                //         rolaIpRequest.getState(), rolaIpRequest.getCity());

                // log.info("rola refresh :{}", result);

                // if (CharSequenceUtil.contains(result, "SUCCESS")) {
                // Thread.sleep(1500);
                List<String> ipPort = CharSequenceUtil.split(rolaProxy, ":");

                Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ipPort.get(0), Integer.parseInt(ipPort.get(1))));

                OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy)
                        .addInterceptor(new GzipRequestInterceptor()).readTimeout(5, TimeUnit.SECONDS).build();
                Request request = new Request.Builder().url(ProxyConstant.LUMTEST_URL).build();

                okhttp3.Response response = client.newCall(request).execute();

                String responseString = response.body().string();

                log.info("lumtest :{}", responseString);
                // if (responseString.contains("No peer available")) {
                //     collectFlag.set(12L);
                //     queue.clear();
                //     redisTemplate.boundValueOps(RedisKeyConstant.ROLA_COLLECT_ERROR_KEY).set("No peer available");
                //     continue;
                // }

                LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);

                // 如果城市 或者 州 为nul 调用http://ip123.in/search_ip?ip=xxx 补齐

                if (CharSequenceUtil.hasBlank(luminatiIPDTO.getGeo().getRegion(),
                        luminatiIPDTO.getGeo().getCity())) {
                    String ip123InfoResult = HttpUtil.get("http://ip123.in/search_ip?ip=" + luminatiIPDTO.getIp());

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

                save(luminatiIPDTO, ip123FraudDTO, usAddress);

                RAtomicLong totalNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_TOTAL_KEY);
                totalNum.incrementAndGet();

                String today = DateUtil.today();
                RAtomicLong todayNum = redissonClient.getAtomicLong("#ROLA_" + today);
                todayNum.incrementAndGet();

            } catch (Exception e) {
                RAtomicLong currentFailNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_CURRENT_FAIL_KEY);
                currentFailNum.incrementAndGet();

                log.error(" ROLA_COLLECT_IP_QUEUE Exception:{}", ExceptionUtil.stacktraceToString(e));
            }

        }
    }


    public void collectByApi() {
        RBlockingQueue<RolaCollectQueueMsgDTO> queue =
                redissonClient.getBlockingQueue(RolaCollectConstant.ROLA_COLLECT_BY_API_QUEUE_KEY);
        while (true) {
            RolaCollectQueueMsgDTO rolaProxy = null;
            try {
                RAtomicLong collectFlag = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_API_FLAG_KEY);
                if (collectFlag.get() == 11L) {
                    // 11 停止
                    Thread.sleep(3000);
                    continue;
                }
                // 10
                rolaProxy = queue.take();

                List<String> ipPort = CharSequenceUtil.split(rolaProxy.getRolaApiIp(), ":");

                Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ipPort.get(0), Integer.parseInt(ipPort.get(1))));

                OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy)
                        .addInterceptor(new GzipRequestInterceptor()).readTimeout(5, TimeUnit.SECONDS).build();
                Request request = new Request.Builder().url(ProxyConstant.LUMTEST_URL).build();

                okhttp3.Response response = client.newCall(request).execute();

                String responseString = response.body().string();


                log.info("lumtest :{}", responseString);
                /*if (responseString.contains("No peer available")) {
                    collectFlag.set(12L);
                    queue.clear();
                    redisTemplate.boundValueOps(RedisKeyConstant.ROLA_COLLECT_ERROR_KEY).set("No peer available");
                    continue;
                }*/

                LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);

                // TODO 流量


                // 如果城市 或者 州 为nul 调用http://ip123.in/search_ip?ip=xxx 补齐
                if (CharSequenceUtil.hasBlank(luminatiIPDTO.getGeo().getRegion(),
                        luminatiIPDTO.getGeo().getCity())) {
                    String ip123InfoResult = HttpUtil.get("http://ip123.in/search_ip?ip=" + luminatiIPDTO.getIp());

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

                save(luminatiIPDTO, ip123FraudDTO, rolaProxy.getRolaAccessServer());

                // RAtomicLong totalNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_TOTAL_KEY);
                // totalNum.incrementAndGet();
                //
                // String today = DateUtil.today();
                // RAtomicLong todayNum = redissonClient.getAtomicLong("#ROLA_" + today);
                // todayNum.incrementAndGet();










            } catch (SocketTimeoutException se) {
                //
                RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                collectResultDTO.setIp(rolaProxy.getRolaApiIp());
                collectResultDTO.setMsg("ROLA IP网络异常，未获取到IP");
                redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_API_RESULT, collectResultDTO);

            } catch (Exception e) {
                // RAtomicLong currentFailNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_CURRENT_FAIL_KEY);
                // currentFailNum.incrementAndGet();

                log.error(" ROLA_COLLECT_BY_API_QUEUE_KEY Exception:{}", ExceptionUtil.stacktraceToString(e));
            }

        }
    }

    private void save(LuminatiIPDTO luminatiIPDTO, Ip123FraudDTO ip123FraudDTO, String source) {

        long count =
                rolaIpMapper.selectCount(new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getIp, luminatiIPDTO.getIp()));
        if (count > 0) {
            log.info("已存在IP: {}", luminatiIPDTO.getIp());

            RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
            collectResultDTO.setIp(luminatiIPDTO.getIp());
            collectResultDTO.setMsg("重复IP");
            redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_API_RESULT, collectResultDTO);

            // RAtomicLong currentRepeatNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_CURRENT_REPEAT_KEY);
            // currentRepeatNum.incrementAndGet();
        } else {
            RolaIpDO rolaIpDO = new RolaIpDO();
            rolaIpDO.setIp(luminatiIPDTO.getIp());
            rolaIpDO.setCountry(luminatiIPDTO.getCountry().toLowerCase());
            rolaIpDO.setRegion(luminatiIPDTO.getGeo().getRegion().toLowerCase());
            rolaIpDO.setCity(luminatiIPDTO.getGeo().getCity().toLowerCase());
            rolaIpDO.setPostalCode(luminatiIPDTO.getGeo().getPostalCode());
            rolaIpDO.setTz(luminatiIPDTO.getGeo().getTz());

            rolaIpDO.setRisk(ip123FraudDTO.getRisk());
            rolaIpDO.setRiskEnglish(ip123FraudDTO.getRiskEnglish());
            rolaIpDO.setScore(ip123FraudDTO.getScore());

            rolaIpDO.setSource(source);
            try {
                rolaIpMapper.insert(rolaIpDO);

                RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                collectResultDTO.setIp(luminatiIPDTO.getIp());
                collectResultDTO.setMsg("成功IP");
                redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_API_RESULT, collectResultDTO);
            } catch (Exception e) {
                RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                collectResultDTO.setIp(luminatiIPDTO.getIp());
                collectResultDTO.setMsg("重复IP");
                redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_API_RESULT, collectResultDTO);
            }
            log.info("插入新数据: {}", rolaIpDO);
        }

    }
}
