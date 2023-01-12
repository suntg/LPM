package com.example.lpm.job;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDateTime;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.lpm.config.LuminatiProperties;
import com.example.lpm.domain.entity.IpAddrDO;
import com.example.lpm.mapper.IpAddrMapper;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.config.AsyncConfig;
import com.example.lpm.constant.RedisKeyConstant;
import com.example.lpm.domain.dto.LuminatiIPDTO;
import com.example.lpm.domain.request.LuminatiCollectIpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@RequiredArgsConstructor
@Component
public class LuminatiCollectRunner implements CommandLineRunner {

    private final RedissonClient redissonClient;

    private final ObjectMapper objectMapper;

    @Resource
    private LuminatiProperties luminatiProperties;
    @Resource
    private IpAddrMapper ipAddrMapper;

    private final AsyncConfig asyncConfig;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 6; i++) {
            asyncConfig.collectLuminatiThreadPool().submit(this::collect);
            Thread.sleep(2000);
        }
    }

    public void collect() {
        RBlockingQueue<LuminatiCollectIpRequest> queue =
            redissonClient.getBlockingQueue(RedisKeyConstant.LUMINATI_COLLECT_IP_QUEUE_KEY);
        while (true) {
            LuminatiCollectIpRequest ipRequest = null;
            try {
                RAtomicLong collectFlag = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_COLLECT_FLAG_KEY);

                if (collectFlag.get() == 11L) {
                    // 11 停止
                    Thread.sleep(3000);
                    continue;
                }
                // 10
                ipRequest = queue.take();

                //
                String url = "http://lumtest.com/myip.json";
                try {
                    String country = ipRequest.getCountry();
                    String state = ipRequest.getState();
                    String city = ipRequest.getCity();
                    HttpResponse response;
                    if (CharSequenceUtil.isAllNotBlank(country, state, city)) {
                        String proxyUsername = CharSequenceUtil.format(
                            "lum-customer-c_99c3c376-zone-zone_city_ly-country-{}-state-{}-city-{}",
                            CharSequenceUtil.cleanBlank(country).toLowerCase(),
                            CharSequenceUtil.cleanBlank(state).toLowerCase(),
                            CharSequenceUtil.cleanBlank(city).toLowerCase());
                        response = HttpRequest.get(url)
                            .setProxy(new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress(luminatiProperties.getProxyHost(),
                                    luminatiProperties.getProxyPort())))
                            .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
                    } else if (CharSequenceUtil.isBlank(country) && CharSequenceUtil.isAllNotBlank(state, city)) {
                        String proxyUsername = CharSequenceUtil.format(
                            "lum-customer-c_99c3c376-zone-zone_city_ly-country-us-state-{}-city-{}",
                            CharSequenceUtil.cleanBlank(state).toLowerCase(),
                            CharSequenceUtil.cleanBlank(city).toLowerCase());
                        response = HttpRequest.get(url)
                            .setProxy(new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress(luminatiProperties.getProxyHost(),
                                    luminatiProperties.getProxyPort())))
                            .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
                    } else if (CharSequenceUtil.isNotBlank(country) && CharSequenceUtil.isNotBlank(state)
                        && CharSequenceUtil.isBlank(city)) {
                        String proxyUsername =
                            CharSequenceUtil.format("lum-customer-c_99c3c376-zone-zone_city_ly-country-{}-state-{}",
                                CharSequenceUtil.cleanBlank(country).toLowerCase(),
                                CharSequenceUtil.cleanBlank(state).toLowerCase());
                        response = HttpRequest.get(url)
                            .setProxy(new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress(luminatiProperties.getProxyHost(),
                                    luminatiProperties.getProxyPort())))
                            .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
                    } else if (CharSequenceUtil.isBlank(country) && CharSequenceUtil.isNotBlank(state)
                        && CharSequenceUtil.isBlank(city)) {
                        String proxyUsername =
                            CharSequenceUtil.format("lum-customer-c_99c3c376-zone-zone_city_ly-country-us-state-{}",
                                CharSequenceUtil.cleanBlank(state).toLowerCase());
                        response = HttpRequest.get(url)
                            .setProxy(new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress(luminatiProperties.getProxyHost(),
                                    luminatiProperties.getProxyPort())))
                            .basicProxyAuth(proxyUsername, "pmix35o1gack").setReadTimeout(5000).execute();
                    } else {
                        response = HttpRequest.get(url)
                            .setProxy(new Proxy(Proxy.Type.HTTP,
                                new InetSocketAddress(luminatiProperties.getProxyHost(),
                                    luminatiProperties.getProxyPort())))
                            .basicProxyAuth(luminatiProperties.getProxyUsername(),
                                luminatiProperties.getProxyPassword())
                            .setReadTimeout(5000).execute();
                    }

                    log.info("getLuminatiIpAddrByAsync lumtest result : {}", response.toString());
                    if (HttpStatus.HTTP_OK == response.getStatus()) {
                        String xLuminatiIP = response.header("x-luminati-ip");

                        String luminatiResult = response.body();
                        log.info("getLuminatiIpAddrByAsync lumtest response body : {}", luminatiResult);

                        if (luminatiResult.contains("No peer available")) {
                            collectFlag.set(12L);
                            queue.clear();
                            redisTemplate.boundValueOps(RedisKeyConstant.LUMINATI_COLLECT_ERROR_KEY).set("No peer available");
                            continue;
                        }

                        LuminatiIPDTO luminatiIPDTO = JSON.parseObject(luminatiResult, LuminatiIPDTO.class);
                        if (CharSequenceUtil.isAllNotEmpty(luminatiIPDTO.getCountry(),
                            luminatiIPDTO.getGeo().getRegion(), luminatiIPDTO.getGeo().getCity())) {
                            log.info("luminatiIPDTO :{}", JSON.toJSONString(luminatiIPDTO));
                            this.saveOrUpdateLuminati(luminatiIPDTO, xLuminatiIP);

                            //总收入
                            RAtomicLong totalNum = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_TOTAL_KEY);
                            totalNum.incrementAndGet();

                            //今日收入
                            String today = DateUtil.today();
                            RAtomicLong todayNum = redissonClient.getAtomicLong("#LUMINATI_" + today);
                            todayNum.incrementAndGet();
                        }

                    } else {
                        log.info("lumtest not ok : {}", response.body());
                    }
                } catch (Exception e) {
                    log.error("lumtest error : {}", ExceptionUtil.stacktraceToString(e));
                }

            } catch (Exception e) {
                RAtomicLong currentFailNum = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_CURRENT_FAIL_KEY);
                currentFailNum.incrementAndGet();

                log.error(" LUMINATI_COLLECT_IP_QUEUE Exception:{}", ExceptionUtil.stacktraceToString(e));
            }

        }
    }

    private void saveOrUpdateLuminati(LuminatiIPDTO luminatiIPDTO, String xLuminatiIP) {
        if (CharSequenceUtil.isAllNotBlank(xLuminatiIP, luminatiIPDTO.getIp(), luminatiIPDTO.getCountry(),
                luminatiIPDTO.getGeo().getCity(), luminatiIPDTO.getGeo().getRegion(),
                luminatiIPDTO.getGeo().getPostalCode())) {
            long count = ipAddrMapper
                    .selectCount(new QueryWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getIp, luminatiIPDTO.getIp()));
            if (count > 0) {
                log.info("已存在IP: {}，更新心跳时间", luminatiIPDTO.getIp());
                ipAddrMapper.update(new IpAddrDO(),
                        new UpdateWrapper<IpAddrDO>().lambda().eq(IpAddrDO::getIp, luminatiIPDTO.getIp())
                                .set(IpAddrDO::getCountry, luminatiIPDTO.getCountry().toLowerCase())
                                .set(IpAddrDO::getRegion, luminatiIPDTO.getGeo().getRegion().toLowerCase())
                                .set(IpAddrDO::getCity, luminatiIPDTO.getGeo().getCity().toLowerCase())
                                .set(IpAddrDO::getLastHeartbeatTime, LocalDateTime.now()));

                RAtomicLong currentRepeatNum = redissonClient.getAtomicLong(RedisKeyConstant.LUMINATI_CURRENT_REPEAT_KEY);
                currentRepeatNum.incrementAndGet();
            } else {
                IpAddrDO ipAddrDO = new IpAddrDO();
                ipAddrDO.setIp(luminatiIPDTO.getIp());
                ipAddrDO.setCountry(luminatiIPDTO.getCountry().toLowerCase());
                ipAddrDO.setRegion(luminatiIPDTO.getGeo().getRegion().toLowerCase());
                ipAddrDO.setCity(luminatiIPDTO.getGeo().getCity().toLowerCase());
                ipAddrDO.setPostalCode(luminatiIPDTO.getGeo().getPostalCode());
                ipAddrDO.setXLuminatiIp(xLuminatiIP);
                ipAddrDO.setState(1);
                ipAddrDO.setLastHeartbeatTime(LocalDateTime.now());
                ipAddrDO.setCreateTime(LocalDateTime.now());
                ipAddrMapper.insert(ipAddrDO);
                log.info("插入新数据: {}", ipAddrDO);
            }
        }
    }


}
