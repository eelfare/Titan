package com.yunji.titanrtx.manager.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2020-01-17 14:13
 * @Version 1.0
 */
@Configuration
public class AsyncConfig {

    @Bean
    public TaskExecutor wokExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("Async-");
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setQueueCapacity(600);
        threadPoolTaskExecutor.afterPropertiesSet();

        // 自定义拒绝策略
        threadPoolTaskExecutor.setRejectedExecutionHandler((r, executor) -> {
            // .....
        });
        // 使用预设的拒绝策略
        threadPoolTaskExecutor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        return threadPoolTaskExecutor;
    }
}
