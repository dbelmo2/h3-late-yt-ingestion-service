package com.h3late.ingestion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class YouTubeSubscriptionTask {
    private final YouTubeSubscriptionService youTubeSubscriptionService;
    public YouTubeSubscriptionTask(
            YouTubeSubscriptionService youTubeSubscriptionService
    ) {
        this.youTubeSubscriptionService = youTubeSubscriptionService;
    }
    @Scheduled(fixedRate = 86400000)
    public void subscribeToChannels() {
        log.info("Starting YouTube channel re-subscription task...");
        youTubeSubscriptionService.subscribe();
    }
}
