package com.example.lpm.v1.job;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.v1.domain.entity.RolaIpDO;
import com.example.lpm.v1.service.RolaIpService;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class RolaIpClearJob {

    private final RolaIpService rolaIpService;

    @Scheduled(initialDelay = 1, fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void clear() {
        rolaIpService.remove(
            new QueryWrapper<RolaIpDO>().lambda().isNotNull(RolaIpDO::getFileFlag).isNotNull(RolaIpDO::getLastUseTime)
                .le(RolaIpDO::getLastUseTime, LocalDateTimeUtil.offset(LocalDateTime.now(), -10, ChronoUnit.DAYS)));

    }
}
