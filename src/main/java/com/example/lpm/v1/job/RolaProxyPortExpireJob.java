package com.example.lpm.v1.job;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.v1.domain.entity.RolaProxyPortDO;
import com.example.lpm.v1.service.RolaProxyPortService;

import cn.hutool.core.date.LocalDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class RolaProxyPortExpireJob {

    private final RolaProxyPortService rolaProxyPortService;

    /**
     * 第一次延迟1秒后执行，之后按fixedRate的规则每60秒执行一次
     */
    @Scheduled(initialDelay = 1, fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void rolaProxyPortExpire() {
        List<RolaProxyPortDO> rolaProxyPortDOList =
            rolaProxyPortService.list(new QueryWrapper<RolaProxyPortDO>().lambda().le(RolaProxyPortDO::getCreateTime,
                LocalDateTimeUtil.offset(LocalDateTime.now(), -1, ChronoUnit.HOURS)));
        for (RolaProxyPortDO rolaProxyPortDO : rolaProxyPortDOList) {
            rolaProxyPortService.deleteProxyPortByPort(rolaProxyPortDO.getProxyPort());
        }
    }
}
