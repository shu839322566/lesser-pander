/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.nantian.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池
 *
 * @author shuyx
 * @date 2020/08/20
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncTaskExecutePool{

    /**
     * 核心线程数
     */
    @Value("${thread.pool.core-pool-size}")
    private int corePoolSize;

    /**
     * 最大线程数
     */
    @Value("${thread.pool.max-pool-size}")
    private int maxPoolSize;

    /**
     * 等待线程数
     */
    @Value("${thread.pool.queue-capacity}")
    private int queueCapacity;

    /**
     * 空闲等待时间, 单位为秒
     */
    @Value("${thread.pool.keep-alive-seconds}")
    private int keepAliveTime;

    /**
     * 线程前缀名称
     */
    @Value("${thread.pool.thread-name-prefix}")
    private String threadNamePrefix;

    @Bean(value = "threadPoolTaskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        // 核心线程池大小
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        // 最大线程数
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        // 队列容量
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        // 活跃时间
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveTime);
        // 线程名字前缀
        threadPoolTaskExecutor.setThreadNamePrefix(threadNamePrefix);
        // 拒绝策略
        // setRejectedExecutionHandler-当pool已经达到maxsize的时候，如何处理新任务
        // CallerRunsPolicy-不在新线程中执行任务，而是由调用者所在的线程来执行
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        // 线程池初始化
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    @Bean(value = "asyncUncaughtExceptionHandler")
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            log.error("====" + throwable.getMessage() + "====", throwable);
            log.error("exception method:" + method.getName());
        };
    }
}
