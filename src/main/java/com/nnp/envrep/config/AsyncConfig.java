/**
 * AsyncConfig.java
 *
 * @author AC
 * @date 28-Apr-2025
 */
package com.nnp.envrep.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * AsyncConfig.java
 *
 * @author AC
 * @date 28-Apr-2025
 */
@Configuration
@EnableAsync
public class AsyncConfig {
	@Bean(name = "taskExecutor") // Spring will pick this by default for @Async
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncGitOpsEvent-");
        executor.initialize();
        return executor;
	}
}
