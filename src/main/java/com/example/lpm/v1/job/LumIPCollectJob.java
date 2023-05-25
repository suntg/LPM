package com.example.lpm.v1.job;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.constant.ProxyConstant;
import com.example.lpm.domain.dto.LuminatiIPDTO;
import com.example.lpm.v1.common.BizException;
import com.example.lpm.v1.common.ReturnCode;
import com.example.lpm.v1.constant.LumIPConstant;
import com.example.lpm.v1.domain.dto.Ip123FraudDTO;
import com.example.lpm.v1.domain.dto.Ip123InfoDTO;
import com.example.lpm.v1.domain.entity.LumIPDO;
import com.example.lpm.v1.domain.request.LumIPCollectRequest;
import com.example.lpm.v1.mapper.LumIPMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Component
@Slf4j
public class LumIPCollectJob {

    private final ExecutorService executorService =
        Executors.newFixedThreadPool(LumIPConstant.LUM_IP_COLLECT_THREAD_SIZE);
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LumIPMapper lumIPMapper;

    @PostConstruct
    public void init() {
        RBlockingQueue<LumIPCollectRequest> queue =
            redissonClient.getBlockingQueue(LumIPConstant.LUM_IP_COLLECT_QUEUE_KEY);
        for (int i = 0; i < LumIPConstant.LUM_IP_COLLECT_THREAD_SIZE; i++) {
            executorService.submit(() -> {
                while (true) {
                    try {
                        LumIPCollectRequest item = queue.take();
                        // 处理队列中的数据
                        String username = null;
                        if (CharSequenceUtil.isBlank(item.getState()) && CharSequenceUtil.isBlank(item.getCity())) {
                            username = CharSequenceUtil.format("brd-customer-{}-zone-{}-country-{}",
                                item.getCustomerUsername(), item.getZoneUsername(), item.getCountry());
                        }
                        if (CharSequenceUtil.isNotBlank(item.getState()) && CharSequenceUtil.isBlank(item.getCity())) {
                            username = CharSequenceUtil.format("brd-customer-{}-zone-{}-country-{}-state-{}",
                                item.getCustomerUsername(), item.getZoneUsername(), item.getCountry(), item.getState());
                        }
                        if (CharSequenceUtil.isNotBlank(item.getState())
                            && CharSequenceUtil.isNotBlank(item.getCity())) {
                            username = CharSequenceUtil.format("brd-customer-{}-zone-{}-country-{}-state-{}-city-{}",
                                item.getCustomerUsername(), item.getZoneUsername(), item.getCountry(), item.getState(),
                                item.getCity());
                        }

                        Proxy proxy =
                            new Proxy(Proxy.Type.HTTP, new InetSocketAddress(LumIPConstant.LUM_IP_PROXY_HOSTNAME,
                                LumIPConstant.LUM_IP_PROXY_PORT));
                        String finalUsername = username;
                        log.info("lum username {}", username);
                        log.info("lum req {}", item);
                        /*java.net.Authenticator.setDefault(new java.net.Authenticator() {
                            private final PasswordAuthentication authentication =
                                new PasswordAuthentication(finalUsername, item.getZonePassword().toCharArray());
                        
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                log.info("auth username {}, auth pass {}", authentication.getUserName(),
                                    authentication.getPassword());
                                return authentication;
                            }
                        });
                        OkHttpClient client = new OkHttpClient().newBuilder().proxy(proxy)
                            .addInterceptor(new GzipRequestInterceptor()).build();*/
                        OkHttpClient client =
                            new OkHttpClient.Builder().proxy(proxy).proxyAuthenticator((route, response) -> {
                                String credential = Credentials.basic(finalUsername, item.getZonePassword());
                                return response.request().newBuilder().header("Proxy-Authorization", credential)
                                    .build();
                            }).build();

                        Request request = new Request.Builder().url(ProxyConstant.LUMTEST_URL).build();

                        try {
                            okhttp3.Response response = client.newCall(request).execute();
                            String xluminatiIP = response.header("x-luminati-ip");
                            String responseString = response.body().string();
                            log.info(responseString);
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

                            save(luminatiIPDTO, ip123FraudDTO, xluminatiIP);

                        } catch (Exception e) {
                            log.error("LUM异常 {}", ExceptionUtil.stacktraceToString(e));
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }

    private void save(LuminatiIPDTO luminatiIPDTO, Ip123FraudDTO ip123FraudDTO, String xluminatiIP) {

        long count =
            lumIPMapper.selectCount(new QueryWrapper<LumIPDO>().lambda().eq(LumIPDO::getIp, luminatiIPDTO.getIp()));
        if (count > 0) {
            log.info("已存在IP: {}", luminatiIPDTO.getIp());
        } else {
            LumIPDO lumIPDO = new LumIPDO();
            lumIPDO.setIp(luminatiIPDTO.getIp());
            lumIPDO.setCountry(luminatiIPDTO.getCountry().toLowerCase());
            lumIPDO.setRegion(luminatiIPDTO.getGeo().getRegion().toLowerCase());
            lumIPDO.setCity(luminatiIPDTO.getGeo().getCity().toLowerCase());
            lumIPDO.setPostalCode(luminatiIPDTO.getGeo().getPostalCode());
            lumIPDO.setTz(luminatiIPDTO.getGeo().getTz());
            lumIPDO.setXLuminatiIp(xluminatiIP);
            lumIPDO.setRisk(ip123FraudDTO.getRisk());
            lumIPDO.setRiskEnglish(ip123FraudDTO.getRiskEnglish());
            lumIPDO.setScore(ip123FraudDTO.getScore());
            lumIPMapper.insert(lumIPDO);
            log.info("插入新数据: {}", lumIPDO);
        }

    }
}
