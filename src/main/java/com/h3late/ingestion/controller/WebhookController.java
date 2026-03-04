package com.h3late.ingestion.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.h3late.ingestion.dto.YouTubeFeedDto;
import com.h3late.ingestion.exception.InvalidSignatureException;
import com.h3late.ingestion.service.KafkaProducerService;
import com.h3late.ingestion.service.SignatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@Slf4j
public class WebhookController {

    private final SignatureService signatureService;
    private final KafkaProducerService kafkaProducerService;
    private final XmlMapper xmlMapper = new XmlMapper();

    public WebhookController(
            SignatureService signatureService,
            KafkaProducerService kafkaProducerService
    ) {
        this.kafkaProducerService = kafkaProducerService;
        this.signatureService = signatureService;
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @GetMapping("/youtube")
    public ResponseEntity<String> handleYouTubeVerification(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.challenge") String challenge
    ) {
        log.info("Received YouTube webhook verification request with mode: {}", mode);
        if ("subscribe".equals(mode)) {
            log.info("Responding to YouTube webhook verification with challenge: {}", challenge);
            return ResponseEntity.ok(challenge);
        } else {
            log.error("Invalid mode received in YouTube webhook verification: {}", mode);
            return ResponseEntity.badRequest().body("Invalid mode");
        }
    }


    @PostMapping("/youtube")
    public ResponseEntity<Void> handleYouTubeWebhook(
            @RequestHeader("X-Hub-Signature") String signature,
            @RequestBody String requestBody
    ) {
        log.debug("Incoming Webhook! Signature: {}", signature); // ADD THIS
        if (!signatureService.isValidSignature(signature, requestBody)) {
            throw new InvalidSignatureException("Invalid signature");
        }

        try {
            YouTubeFeedDto feedDto = xmlMapper.readValue(requestBody, YouTubeFeedDto.class);
            if (feedDto != null && feedDto.getEntry() != null) {
                log.info("Processing video event: {}", feedDto.getEntry().getVideoId());
                kafkaProducerService.publishVideoEvent(
                        feedDto.getEntry().getVideoId(),
                        feedDto.getEntry()
                );
            } else if (feedDto != null && feedDto.getDeletedEntry() != null) {
                log.info("Processing video deletion event: {}", feedDto.getDeletedEntry().getVideoId());
                kafkaProducerService.publishVideoDeleteEvent(
                        feedDto.getDeletedEntry().getVideoId()
                );
            } else {
                log.warn("Received YouTube webhook with no recognizable entry or deletedEntry. Ignoring.");
            }
        } catch (Exception e) {
            // Log the error so you can see if the XML structure itself is the issue
            log.error("Failed to parse YouTube XML", e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.noContent().build();
    }
}
