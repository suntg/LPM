package com.example.lpm.v3.job;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.domain.entity.RolaProxyPortDO;
import com.example.lpm.service.RolaProxyPortService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Configuration
@EnableScheduling
public class RolaJob {

    @Resource
    private RolaProxyPortService rolaProxyPortService;


    /**
     * 第一次延迟1秒后执行，之后按fixedRate的规则每60秒执行一次
     */
    @Scheduled(initialDelay = 1000, fixedRate = 60000)
    public void deletePort() {
        List<RolaProxyPortDO> rolaProxyPortDOList = rolaProxyPortService.list(new QueryWrapper<RolaProxyPortDO>().lambda()
                .le(RolaProxyPortDO::getCreateTime, LocalDateTimeUtil.offset(LocalDateTime.now(), -1, ChronoUnit.HOURS)));
        for (RolaProxyPortDO rolaProxyPortDO : rolaProxyPortDOList) {
            rolaProxyPortService.deleteProxyPortByPort(rolaProxyPortDO.getProxyPort());
        }
    }
}
