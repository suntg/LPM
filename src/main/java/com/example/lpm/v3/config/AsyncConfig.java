package com.example.lpm.v3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(value = "collectRolaThreadPool")
    public AsyncTaskExecutor collectRolaThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(55);
        taskExecutor.setCorePoolSize(50);
        taskExecutor.setThreadNamePrefix("collect-rola-ip-task-thread-pool-");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean(value = "phoneCollectRolaThreadPool")
    public AsyncTaskExecutor phoneCollectRolaThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(8);
        taskExecutor.setCorePoolSize(6);
        taskExecutor.setThreadNamePrefix("phone-collect-rola-ip-task-thread-pool-");
        taskExecutor.initialize();
        return taskExecutor;
    }

}
