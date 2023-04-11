package com.example.lpm.v3.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.v3.constant.RolaCollectConstant;
import com.example.lpm.v3.domain.entity.RolaIpDO;
import com.example.lpm.v3.service.RolaIpService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

// @Component
public class RolaIpBloomFilter {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RolaIpService rolaIpService;

    @PostConstruct
    public void run() {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(RolaCollectConstant.ROLA_IP_BLOOM);
        bloomFilter.tryInit(500000L, 0.001);
        List<RolaIpDO> rolaIpDOList = rolaIpService.getBaseMapper().selectList(new QueryWrapper<RolaIpDO>().select("ip"));
        for (RolaIpDO rolaIpDO : rolaIpDOList) {
            bloomFilter.add(rolaIpDO.getIp());
        }
    }

}
