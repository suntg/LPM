package com.example.lpm.v3.addIp.strategy;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.lpm.v3.common.BizException;
import com.example.lpm.v3.common.ReturnCode;
import com.example.lpm.v3.domain.request.CollectionTaskRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class LuminatiAddIpStrategy implements AddProxyIp {

    private final RedissonClient redissonClient;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void addProxyIpTask(CollectionTaskRequest collectionTaskRequest) {
        // 判断 队列数量，>0拒绝任务
        RBlockingQueue<CollectionTaskRequest> queue =
            redissonClient.getBlockingQueue(com.example.lpm.v3.constant.RedisKeyConstant.ADD_PROXY_IP_TASK_TOPIC
                + collectionTaskRequest.getProxyIpType().getTypeName());
        // 放入队列
        for (int i = 0; i < collectionTaskRequest.getNumber(); i++) {
            queue.offerAsync(collectionTaskRequest);
        }
    }

}
