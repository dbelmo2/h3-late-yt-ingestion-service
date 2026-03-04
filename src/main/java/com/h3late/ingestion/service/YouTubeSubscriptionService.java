package com.h3late.ingestion.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class YouTubeSubscriptionService {

    private final RestClient restClient = RestClient.create();

    @Value("${youtube.channel-id}")
    private String channelId;

    @Value("${youtube.callback-url}")
    private String callbackUrl;

    @Value("${youtube.webhook-secret}")
    private String webhookSecret;

    private final String HUB_URL = "https://pubsubhubbub.appspot.com/subscribe";
    private final String TOPIC_BASE_URL = "https://www.youtube.com/xml/feeds/videos.xml?channel_id=";

    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() {
        String topicUrl = TOPIC_BASE_URL + channelId;
        log.info("Attempting to subscribe to channel {} with callback {} using topicUrl {}", channelId, callbackUrl, topicUrl);

        // WebSub requires form-urlencoded data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("hub.mode", "subscribe");
        formData.add("hub.topic", topicUrl);
        formData.add("hub.callback", callbackUrl);
        formData.add("hub.verify", "async");
        formData.add("hub.secret", webhookSecret);

        try {
            restClient.post()
                    .uri(HUB_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toBodilessEntity(); // Google returns 204 No Content on success

            log.info("Successfully sent subscription request for channel: {}", channelId);
        } catch (Exception e) {
            log.error("Failed to subscribe to YouTube Webhook: {}", e.getMessage());
        }
    }



}