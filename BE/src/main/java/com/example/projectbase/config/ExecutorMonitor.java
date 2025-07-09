package com.example.projectbase.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Configuration
@Log4j2
@Component
public class ExecutorMonitor {

    private final ThreadPoolTaskExecutor videoExecutor;
    private final ThreadPoolTaskExecutor audioExecutor;
    private final ThreadPoolTaskExecutor imageExecutor;

    public ExecutorMonitor(
            @Qualifier("rawVideoExecutor") ThreadPoolTaskExecutor videoExecutor,
            @Qualifier("rawAudioExecutor") ThreadPoolTaskExecutor audioExecutor,
            @Qualifier("rawImageExecutor") ThreadPoolTaskExecutor imageExecutor
    ) {
        this.videoExecutor = videoExecutor;
        this.audioExecutor = audioExecutor;
        this.imageExecutor = imageExecutor;
    }

    public void logExecutorStatus(String threadTypeName, ThreadPoolTaskExecutor executorType) {
        log.info("[{} Excutor] - Active: {}, Pool size: {}, Max: {}, Queue: {}",
                threadTypeName,
                executorType.getActiveCount(),
                executorType.getCorePoolSize(),
                executorType.getMaxPoolSize(),
                executorType.getThreadPoolExecutor().getQueue().size());
    }



    @Scheduled(fixedRate = 600000) // 60,000ms = 1 phút
    public void logExecutorStatus() {
        logExecutorStatus("VIDEO", videoExecutor);
        logExecutorStatus("AUDIO", audioExecutor);
        logExecutorStatus("IMAGE", imageExecutor);
    }
}
