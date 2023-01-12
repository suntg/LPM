package com.example.lpm.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;

import com.example.lpm.constant.RedisKeyConstant;
import com.example.lpm.domain.dto.ProxyDelayDTO;
import com.example.lpm.domain.entity.ProxyServerInfoDO;
import com.example.lpm.domain.request.DeleteProxyPortRequest;
import com.example.lpm.mapper.ProxyServerInfoMapper;
import com.example.lpm.service.LuminatiIPService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
// @Component
public class ProxyDelayDeleteJob implements CommandLineRunner {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ProxyServerInfoMapper proxyServerInfoMapper;

    @Resource
    private LuminatiIPService luminatiIPService;

    @Override
    public void run(String... args) throws Exception {
        // Thread delayDeleteThread = new Thread(this::delayDelete);
        // delayDeleteThread.setName("delayDeleteThread");
        // delayDeleteThread.start();

    }

    public void delayDelete() {
        RBlockingQueue<ProxyDelayDTO> blockingQueue =
            redissonClient.getBlockingQueue(RedisKeyConstant.START_PROXY_DELAYED_QUEUE_KEY);
        RDelayedQueue<ProxyDelayDTO> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        while (true) {
            try {
                ProxyDelayDTO proxyDelayDTO = blockingQueue.take();
                RLock lock = redissonClient.getLock(RedisKeyConstant.START_PROXY_KEY + proxyDelayDTO.getServerId());
                lock.lock(5, TimeUnit.SECONDS);
                try {
                    log.info("延迟删除端口: {}", proxyDelayDTO);
                    ProxyServerInfoDO proxyServerInfo = proxyServerInfoMapper.selectById(proxyDelayDTO.getServerId());

                    DeleteProxyPortRequest deleteProxyPortRequest = new DeleteProxyPortRequest();
                    List<Integer> ports = new ArrayList<>();
                    ports.add(proxyDelayDTO.getServerPort());
                    deleteProxyPortRequest.setPorts(ports);
                    deleteProxyPortRequest.setServer(proxyServerInfo.getServerIp());
                    luminatiIPService.deleteProxyPort(deleteProxyPortRequest);
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("START_PROXY_DELAYED_QUEUE延迟队列消费线程InterruptedException:[{}]",
                    ExceptionUtils.getStackTrace(e));
            }

        }
    }
}
