package com.example.lpm.v3.job;


import com.example.lpm.v3.domain.entity.RolaConfigDO;
import com.example.lpm.v3.mapper.RolaConfigMapper;
import com.example.lpm.v3.service.RolaIpService;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RolaLoadData {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RolaIpService rolaIpService;

    @Resource
    private RolaConfigMapper rolaConfigMapper;

    public static final ConcurrentHashMap<Integer, RolaConfigDO> CACHE = new ConcurrentHashMap();

    @PostConstruct
    public void run() {
        /*RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(RolaCollectConstant.ROLA_IP_BLOOM);
        bloomFilter.tryInit(500000L, 0.001);
        List<RolaIpDO> rolaIpDOList = rolaIpService.getBaseMapper().selectList(new QueryWrapper<RolaIpDO>().select("ip"));
        for (RolaIpDO rolaIpDO : rolaIpDOList) {
            bloomFilter.add(rolaIpDO.getIp());
        }*/
        RolaConfigDO rolaConfigDO = rolaConfigMapper.selectById(1);
        CACHE.put(rolaConfigDO.getId(), rolaConfigDO);
    }

}
