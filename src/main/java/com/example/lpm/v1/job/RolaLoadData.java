package com.example.lpm.v1.job;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.v1.constant.RolaCollectConstant;
import com.example.lpm.v1.domain.entity.RolaConfigDO;
import com.example.lpm.v1.domain.entity.RolaIpDO;
import com.example.lpm.v1.mapper.RolaConfigMapper;
import com.example.lpm.v1.service.RolaIpService;

@Component
public class RolaLoadData {

    public static final ConcurrentHashMap<Integer, RolaConfigDO> CACHE = new ConcurrentHashMap();
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RolaIpService rolaIpService;
    @Resource
    private RolaConfigMapper rolaConfigMapper;

    @PostConstruct
    public void run() {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(RolaCollectConstant.ROLA_IP_BLOOM);
        bloomFilter.tryInit(500000L, 0.001);
        List<RolaIpDO> rolaIpDOList =
            rolaIpService.getBaseMapper().selectList(new QueryWrapper<RolaIpDO>().select("ip"));
        for (RolaIpDO rolaIpDO : rolaIpDOList) {
            bloomFilter.add(rolaIpDO.getIp());
        }
        RolaConfigDO rolaConfigDO = rolaConfigMapper.selectById(1);
        CACHE.put(rolaConfigDO.getId(), rolaConfigDO);
    }

}
