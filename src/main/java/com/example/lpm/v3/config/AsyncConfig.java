package com.example.lpm.v3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {

    /*@Bean(value = "collectRolaThreadPool")
    public AsyncTaskExecutor collectRolaThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(80);
        taskExecutor.setCorePoolSize(69);
        taskExecutor.setThreadNamePrefix("collect-rola-ip-task-thread-pool-");
        taskExecutor.initialize();
        return taskExecutor;
    }
*/

    @Bean(value = "rolaCollectByApiThreadPool")
    public AsyncTaskExecutor rolaCollectByApiThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(30);
        taskExecutor.setCorePoolSize(30);
        taskExecutor.setThreadNamePrefix("rola-collect-by-api-task-thread-pool-");
        taskExecutor.initialize();
        return taskExecutor;
    }


    @Bean(value = "rolaCollectBySidThreadPool")
    public AsyncTaskExecutor rolaCollectBySidThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(50);
        taskExecutor.setCorePoolSize(50);
        taskExecutor.setThreadNamePrefix("rola-collect-by-sid-task-thread-pool-");
        taskExecutor.initialize();
        return taskExecutor;
    }


    @Bean(value = "phoneCollectRolaThreadPool")
    public AsyncTaskExecutor phoneCollectRolaThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setThreadNamePrefix("phone-collect-rola-ip-task-thread-pool-");
        taskExecutor.initialize();
        return taskExecutor;
    }

}
