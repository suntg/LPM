package com.example.lpm.job;

import java.util.Date;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.domain.entity.RolaProxyPortDO;
import com.example.lpm.mapper.RolaProxyPortMapper;
import com.example.lpm.service.RolaProxyPortService;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class RolaProxyPortExpireJob {

    private final RolaProxyPortMapper rolaProxyPortMapper;

    private final RolaProxyPortService rolaProxyPortService;

    @Scheduled(initialDelay = 3000, fixedDelay = 100000)
    public void rolaProxyPortExpire() {
        // 查询过期时间<= 当前时间-1h

        Date date = DateUtil.offsetHour(DateTime.now(), -1);
        List<RolaProxyPortDO> rolaProxyPortDOList = rolaProxyPortMapper.selectList(new QueryWrapper<RolaProxyPortDO>()
            .lambda().le(RolaProxyPortDO::getExpirationTime, DateUtil.toLocalDateTime(date)));
        for (RolaProxyPortDO rolaProxyPortDO : rolaProxyPortDOList) {
            rolaProxyPortService.deleteProxyPort(rolaProxyPortDO.getId());
        }
    }
}
