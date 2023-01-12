package com.example.lpm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean("getLuminatiIpTaskExecutor")
    public AsyncTaskExecutor getLuminatiIpTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(3);
        taskExecutor.setCorePoolSize(1);
        taskExecutor.setThreadNamePrefix("luminati-async-task-thread-pool");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean("timedHeartbeatTaskExecutor")
    public AsyncTaskExecutor heartbeatTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(3);
        taskExecutor.setCorePoolSize(1);
        taskExecutor.setThreadNamePrefix("timed-heartbeat-async-task-thread-pool");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean("manualHeartbeatTaskExecutor")
    public AsyncTaskExecutor manualHeartbeatTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(3);
        taskExecutor.setCorePoolSize(1);
        taskExecutor.setThreadNamePrefix("manual-heartbeat-async-task-thread-pool");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean(value = "collectRolaThreadPool")
    public AsyncTaskExecutor collectRolaThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(12);
        taskExecutor.setCorePoolSize(2);
        taskExecutor.setThreadNamePrefix("collect-rola-ip-task-thread-pool-");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean(value = "phoneCollectRolaThreadPool")
    public AsyncTaskExecutor phoneCollectRolaThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setCorePoolSize(2);
        taskExecutor.setThreadNamePrefix("phone-collect-rola-ip-task-thread-pool-");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean(value = "collectLuminatiThreadPool")
    public AsyncTaskExecutor collectLuminatiThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(6);
        taskExecutor.setCorePoolSize(2);
        taskExecutor.setThreadNamePrefix("collect-luminati-ip-task-thread-pool-");
        taskExecutor.initialize();
        return taskExecutor;
    }

}
