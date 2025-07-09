package com.example.projectbase.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Log4j2
@Configuration
@EnableAsync
public class AsyncConfig {

    private final ThreadPoolTaskExecutor videoExecutor = new ThreadPoolTaskExecutor();
    private final ThreadPoolTaskExecutor audioExecutor = new ThreadPoolTaskExecutor();
    private final ThreadPoolTaskExecutor imageExecutor = new ThreadPoolTaskExecutor();

    @Bean(name = "videoProcessingExecutor")
    public AsyncTaskExecutor videoProcessingExecutor() {

        videoExecutor.setCorePoolSize(5);
        videoExecutor.setMaxPoolSize(8);
        videoExecutor.setKeepAliveSeconds(300);
        videoExecutor.setQueueCapacity(50);

        videoExecutor.setWaitForTasksToCompleteOnShutdown(true);
        videoExecutor.setAwaitTerminationSeconds(300);

        videoExecutor.setThreadNamePrefix("video-async-");
        videoExecutor.setTaskDecorator(runnable -> () -> {
            String threadName = Thread.currentThread().getName();
            log.info("Starting video processing on thread: {}", threadName);
            try {
                runnable.run();
            } finally {
                log.info("Completed video processing on thread {}", threadName);
            }
        });
        videoExecutor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(videoExecutor);
    }

    @Bean(name = "imageProcessingExecutor")
    public AsyncTaskExecutor imageProcessingExecutor() {

        imageExecutor.setCorePoolSize(10);
        imageExecutor.setMaxPoolSize(27);
        imageExecutor.setKeepAliveSeconds(180);
        imageExecutor.setQueueCapacity(100);

        imageExecutor.setWaitForTasksToCompleteOnShutdown(true);
        imageExecutor.setAwaitTerminationSeconds(180);

        imageExecutor.setThreadNamePrefix("image-async-");
        imageExecutor.setTaskDecorator(runnable -> () -> {
            String threadName = Thread.currentThread().getName();
            log.info("Starting image processing on thread: {}", threadName);
            try {
                runnable.run();
            } finally {
                log.info("Completed image processing on thread {}", threadName);
            }
        });
        imageExecutor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(imageExecutor);
    }

    @Bean(name = "audioProcessingExecutor")
    public AsyncTaskExecutor audioProcessingExecutor() {

        audioExecutor.setCorePoolSize(10);
        audioExecutor.setMaxPoolSize(15);
        audioExecutor.setKeepAliveSeconds(300);
        audioExecutor.setQueueCapacity(50);

        audioExecutor.setWaitForTasksToCompleteOnShutdown(true);
        audioExecutor.setAwaitTerminationSeconds(300);

        audioExecutor.setThreadNamePrefix("audio-async-");
        audioExecutor.setTaskDecorator(runnable -> () -> {
            String threadName = Thread.currentThread().getName();
            log.info("Starting audio processing on thread: {}", threadName);
            try {
                runnable.run();
            } finally {
                log.info("Completed audio processing on thread {}", threadName);
            }
        });
        audioExecutor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(audioExecutor);
    }

    @Bean(name = "rawVideoExecutor")
    public ThreadPoolTaskExecutor rawVideoExecutor() {
        return videoExecutor;
    }

    @Bean(name = "rawAudioExecutor")
    public ThreadPoolTaskExecutor rawAudioExecutor() {
        return audioExecutor;
    }

    @Bean(name = "rawImageExecutor")
    public ThreadPoolTaskExecutor rawImageExecutor() {
        return imageExecutor;
    }
}
