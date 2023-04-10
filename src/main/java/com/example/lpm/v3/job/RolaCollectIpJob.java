package com.example.lpm.v3.job;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.constant.ProxyConstant;
import com.example.lpm.domain.dto.LuminatiIPDTO;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.v3.common.ReturnCode;
import com.example.lpm.v3.config.AsyncConfig;
import com.example.lpm.v3.config.GzipRequestInterceptor;
import com.example.lpm.v3.constant.RolaCollectConstant;
import com.example.lpm.v3.domain.dto.*;
import com.example.lpm.v3.domain.entity.RolaIpDO;
import com.example.lpm.v3.domain.entity.TrafficDO;
import com.example.lpm.v3.domain.request.RolaCollectRequest;
import com.example.lpm.v3.mapper.RolaIpMapper;
import com.example.lpm.v3.service.TrafficService;
import com.example.lpm.v3.util.OkHttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.net.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class RolaCollectIpJob implements CommandLineRunner {

    private final RedissonClient redissonClient;

    private final ObjectMapper objectMapper;

    private final RolaIpMapper rolaIpMapper;

    private final AsyncConfig asyncConfig;

    private final RedisTemplate<String, Object> redisTemplate;

    private final TrafficService trafficService;

    @Value("${rola.proxy-password}")
    private String rolaProxyPassword;

    @Value("${rola.sid-username}")
    private String rolaSidUsername;

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 50; i++) {
            asyncConfig.rolaCollectByApiThreadPool().submit(this::collectByApi);
            Thread.sleep(2000);
        }

        for (int i = 0; i < 400; i++) {
            asyncConfig.rolaCollectBySidThreadPool().submit(this::collectBySid);
            Thread.sleep(2000);
        }
    }


    public void collectBySid() {

        RBlockingQueue<RolaCollectRequest> queue = redissonClient.getBlockingQueue(RolaCollectConstant.ROLA_COLLECT_BY_SID_QUEUE_KEY);
        while (true) {
            RolaCollectRequest rolaProxy = null;
            StringBuilder userSb = null;
            try {
                RAtomicLong collectFlag = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_SID_FLAG_KEY);
                if (collectFlag.get() == 11L) {
                    // 11 停止
                    Thread.sleep(3000);
                    continue;
                }
                // 10
                rolaProxy = queue.take();


                List<String> ipPort = CharSequenceUtil.split(rolaProxy.getAccessServer().getServerAddr(), ":");


                userSb = new StringBuilder();
                userSb.append(rolaSidUsername).append("-country-").append(rolaProxy.getCountry());
                if (CharSequenceUtil.isNotBlank(rolaProxy.getState())) {
                    userSb.append("-state-").append(rolaProxy.getState());
                }
                if (CharSequenceUtil.isNotBlank(rolaProxy.getCity())) {
                    userSb.append("-city-").append(rolaProxy.getCity());
                }
                userSb.append("-sid-").append(IdUtil.getSnowflake().nextId());

                log.info("sid user:{}", userSb);

                StringBuilder finalUserSb = userSb;
                java.net.Authenticator.setDefault(new java.net.Authenticator() {
                    private PasswordAuthentication authentication =
                            new PasswordAuthentication(finalUserSb.toString(), rolaProxyPassword.toCharArray());

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return authentication;
                    }
                });

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(3, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)
                        .proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ipPort.get(0), Integer.parseInt(ipPort.get(1)))))
                        .build();


                Request request = new Request.Builder().url(RolaCollectConstant.IP234_URL).build();

                okhttp3.Response response = client.newCall(request).execute();
                long bytes = OkHttpUtil.measureTotalBytes(request, response);
                TrafficDO trafficDO = new TrafficDO();
                trafficDO.setUsername(rolaSidUsername);
                trafficDO.setBytes(bytes);
                trafficService.insert(trafficDO);

                String responseString = response.body().string();

                log.info("lumtest :{}", responseString);

                Ip234DTO ip234DTO = objectMapper.readValue(responseString, Ip234DTO.class);

                if (CharSequenceUtil.hasBlank(ip234DTO.getRegion(), ip234DTO.getCity())) {
                    String ip123InfoResult = HttpUtil.get("http://ip123.in/search_ip?ip=" + ip234DTO.getIp());

                    JsonNode jsonNode = objectMapper.readTree(ip123InfoResult);

                    Ip123InfoDTO ip123InfoDTO = objectMapper.readValue(jsonNode.get("data").toString(), Ip123InfoDTO.class);

                    ip234DTO.setRegion(ip123InfoDTO.getRegionCode());
                    ip234DTO.setCity(ip123InfoDTO.getCity());
                    ip234DTO.setPostal(ip123InfoDTO.getPostal());
                }
                if (CharSequenceUtil.hasBlank(ip234DTO.getRegion(), ip234DTO.getCity())) {
                    throw new BizException(ReturnCode.RC999.getCode(), ReturnCode.RC999.getMessage());
                }

                String ip123FraudResult = HttpUtil.get("http://www.ip123.in/fraud_check?ip=" + ip234DTO.getIp());
                JsonNode jsonNode = objectMapper.readTree(ip123FraudResult);
                Ip123FraudDTO ip123FraudDTO = objectMapper.readValue(jsonNode.get("data").toString(), Ip123FraudDTO.class);


                long count = rolaIpMapper.selectCount(new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getIp, ip234DTO.getIp()));
                if (count > 0) {
                    log.info("已存在IP: {}", ip234DTO.getIp());

                    RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                    collectResultDTO.setIp(ip234DTO.getIp());
                    collectResultDTO.setMsg("重复IP");
                    redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_SID_RESULT, collectResultDTO);

                    RAtomicLong currentRepeatNum = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_SID_DUPLICATE_NUM);
                    currentRepeatNum.incrementAndGet();
                } else {
                    RolaIpDO rolaIpDO = new RolaIpDO();
                    rolaIpDO.setIp(ip234DTO.getIp());
                    rolaIpDO.setCountry(ip234DTO.getCountryCode().toLowerCase());
                    rolaIpDO.setRegion(ip234DTO.getRegionCode().toLowerCase());
                    rolaIpDO.setCity(ip234DTO.getCity().toLowerCase());
                    rolaIpDO.setPostalCode(ip234DTO.getPostal());
                    rolaIpDO.setTz(ip234DTO.getTimezone());

                    rolaIpDO.setRisk(ip123FraudDTO.getRisk());
                    rolaIpDO.setRiskEnglish(ip123FraudDTO.getRiskEnglish());
                    rolaIpDO.setScore(ip123FraudDTO.getScore());

                    rolaIpDO.setSource(rolaProxy.getAccessServer().getServerAddr());
                    try {
                        rolaIpMapper.insert(rolaIpDO);

                        RAtomicLong successNum = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_SID_SUCCESS_NUM);
                        successNum.incrementAndGet();


                        RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                        collectResultDTO.setIp(ip234DTO.getIp());
                        collectResultDTO.setMsg("成功IP");
                        redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_SID_RESULT, collectResultDTO);
                    } catch (Exception e) {
                        RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                        collectResultDTO.setIp(ip234DTO.getIp());
                        collectResultDTO.setMsg("重复IP");
                        redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_SID_RESULT, collectResultDTO);

                        RAtomicLong currentRepeatNum = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_SID_DUPLICATE_NUM);
                        currentRepeatNum.incrementAndGet();
                    }
                    log.info("插入新数据: {}", rolaIpDO);
                }
            } catch (SocketTimeoutException | SocketException se) {
                RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                collectResultDTO.setIp(userSb.toString());
                collectResultDTO.setMsg("请求失败，未获取到IP");
                redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_SID_RESULT, collectResultDTO);

                RAtomicLong failNum = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_SID_FAIL_NUM);
                failNum.incrementAndGet();

                log.error(" SocketException SocketTimeoutException:{}", ExceptionUtil.stacktraceToString(se));
            } catch (InterruptedException ie) {
                log.error(" ROLA_COLLECT_BY_API_QUEUE_KEY interruptedException:{}", ExceptionUtil.stacktraceToString(ie));
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                collectResultDTO.setIp(userSb.toString());
                collectResultDTO.setMsg("请求失败，未获取到IP");
                redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_SID_RESULT, collectResultDTO);


                RAtomicLong failNum = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_SID_FAIL_NUM);
                failNum.incrementAndGet();
                log.error(" ROLA_COLLECT_BY_SID_QUEUE_KEY Exception:{}", ExceptionUtil.stacktraceToString(e));
            }
        }


    }


    public void collectByApi() {
        RBlockingQueue<RolaCollectQueueMsgDTO> queue = redissonClient.getBlockingQueue(RolaCollectConstant.ROLA_COLLECT_BY_API_QUEUE_KEY);
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

                OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy).addInterceptor(new GzipRequestInterceptor())
                        .readTimeout(5, TimeUnit.SECONDS).build();
                Request request = new Request.Builder().url(ProxyConstant.LUMTEST_URL).build();

                Response response = client.newCall(request).execute();

                long bytes = OkHttpUtil.measureTotalBytes(request, response);
                TrafficDO trafficDO = new TrafficDO();
                trafficDO.setUsername(rolaProxy.getUsername());
                trafficDO.setBytes(bytes);
                trafficService.insert(trafficDO);

                String responseString = response.body().string();


                log.info("lumtest :{}", responseString);
                /*if (responseString.contains("No peer available")) {
                    collectFlag.set(12L);
                    queue.clear();
                    redisTemplate.boundValueOps(RedisKeyConstant.ROLA_COLLECT_ERROR_KEY).set("No peer available");
                    continue;
                }*/

                LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);


                // 如果城市 或者 州 为nul 调用http://ip123.in/search_ip?ip=xxx 补齐
                if (CharSequenceUtil.hasBlank(luminatiIPDTO.getGeo().getRegion(), luminatiIPDTO.getGeo().getCity())) {
                    String ip123InfoResult = HttpUtil.get("http://ip123.in/search_ip?ip=" + luminatiIPDTO.getIp());

                    JsonNode jsonNode = objectMapper.readTree(ip123InfoResult);

                    Ip123InfoDTO ip123InfoDTO = objectMapper.readValue(jsonNode.get("data").toString(), Ip123InfoDTO.class);

                    luminatiIPDTO.getGeo().setRegion(ip123InfoDTO.getRegionCode());
                    luminatiIPDTO.getGeo().setCity(ip123InfoDTO.getCity());
                    luminatiIPDTO.getGeo().setPostalCode(ip123InfoDTO.getPostal());
                }
                if (CharSequenceUtil.hasBlank(luminatiIPDTO.getGeo().getRegion(), luminatiIPDTO.getGeo().getCity())) {
                    throw new BizException(ReturnCode.RC999.getCode(), ReturnCode.RC999.getMessage());
                }
                String ip123FraudResult = HttpUtil.get("http://www.ip123.in/fraud_check?ip=" + luminatiIPDTO.getIp());
                JsonNode jsonNode = objectMapper.readTree(ip123FraudResult);
                Ip123FraudDTO ip123FraudDTO = objectMapper.readValue(jsonNode.get("data").toString(), Ip123FraudDTO.class);

                long count = rolaIpMapper.selectCount(new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getIp, luminatiIPDTO.getIp()));
                if (count > 0) {
                    log.info("已存在IP: {}", luminatiIPDTO.getIp());

                    RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                    collectResultDTO.setIp(luminatiIPDTO.getIp());
                    collectResultDTO.setMsg("重复IP");
                    redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_API_RESULT, collectResultDTO);

                    RAtomicLong currentRepeatNum = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_API_DUPLICATE_NUM);
                    currentRepeatNum.incrementAndGet();
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

                    rolaIpDO.setSource(rolaProxy.getRolaAccessServer());
                    try {
                        rolaIpMapper.insert(rolaIpDO);

                        RAtomicLong successNum = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_API_SUCCESS_NUM);
                        successNum.incrementAndGet();


                        RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                        collectResultDTO.setIp(luminatiIPDTO.getIp());
                        collectResultDTO.setMsg("成功IP");
                        redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_API_RESULT, collectResultDTO);
                    } catch (Exception e) {
                        RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                        collectResultDTO.setIp(luminatiIPDTO.getIp());
                        collectResultDTO.setMsg("重复IP");
                        redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_API_RESULT, collectResultDTO);

                        RAtomicLong currentRepeatNum = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_API_DUPLICATE_NUM);
                        currentRepeatNum.incrementAndGet();
                    }
                    log.info("插入新数据: {}", rolaIpDO);
                }
            } catch (SocketTimeoutException | SocketException se) {
                RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                collectResultDTO.setIp(rolaProxy.getRolaApiIp());
                collectResultDTO.setMsg("请求失败，未获取到IP");
                redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_API_RESULT, collectResultDTO);

                RAtomicLong failNum = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_API_FAIL_NUM);
                failNum.incrementAndGet();

                log.error(" SocketException SocketTimeoutException:{}", ExceptionUtil.stacktraceToString(se));
            } catch (InterruptedException ie) {
                log.error(" ROLA_COLLECT_BY_API_QUEUE_KEY interruptedException:{}", ExceptionUtil.stacktraceToString(ie));
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                RolaCollectResultDTO collectResultDTO = new RolaCollectResultDTO();
                collectResultDTO.setIp(rolaProxy.getRolaApiIp());
                collectResultDTO.setMsg("请求失败，未获取到IP");
                redisTemplate.opsForList().leftPush(RolaCollectConstant.ROLA_COLLECT_BY_API_RESULT, collectResultDTO);


                RAtomicLong failNum = redissonClient.getAtomicLong(RolaCollectConstant.ROLA_COLLECT_BY_API_FAIL_NUM);
                failNum.incrementAndGet();
                log.error(" ROLA_COLLECT_BY_API_QUEUE_KEY Exception:{}", ExceptionUtil.stacktraceToString(e));
            }

        }
    }
}
