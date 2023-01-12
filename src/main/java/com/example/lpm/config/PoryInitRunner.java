package com.example.lpm.config;

import javax.annotation.Resource;

import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Component
public class PoryInitRunner implements ApplicationRunner {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        RQueue<Integer> queue = redissonClient.getQueue("Port_Queue");
        if (queue.isEmpty()) {
            for (int i = 40000; i < 50000; i++) {
                queue.add(i);
            }
        }

        RQueue<Integer> rolaPortQueue = redissonClient.getQueue("Check_Rola_Port_Queue");
        if (rolaPortQueue.isEmpty()) {
            for (int i = 64000; i < 65000; i++) {
                rolaPortQueue.add(i);
            }
        }

    }
}
