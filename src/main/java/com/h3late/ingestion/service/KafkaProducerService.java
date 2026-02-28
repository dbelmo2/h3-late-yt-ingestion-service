package com.h3late.ingestion.service;

import com.h3late.ingestion.dto.YouTubeFeedDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaProducerService(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishVideoEvent(String videoId, YouTubeFeedDto.Entry messageBody) {
        this.kafkaTemplate.send(
                this.topic,
                videoId,
                messageBody
        ).whenComplete((
                result,
                ex) -> {
            if (ex == null) {
                log.info("Sent message=[{}] with offset=[{}]", messageBody.getTitle(), result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send message=[{}] due to : {}", messageBody.getTitle(), ex.getMessage());
            }
        });
    }

}
