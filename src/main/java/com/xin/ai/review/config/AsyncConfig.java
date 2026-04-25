package com.xin.ai.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author xyf527
 * @version 1.0
 * @description
 * @date 2026-04-22 20:45
 * @github https://github.com/xyf527
 * @copyright
 */

@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(20);
        // 等待队列大小
        executor.setQueueCapacity(100);
        // 线程名前缀 方便排查
        executor.setThreadNamePrefix("ar-review-");
        executor.initialize();
        return executor;
    }

}
