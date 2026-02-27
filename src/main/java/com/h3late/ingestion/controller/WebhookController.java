package com.h3late.ingestion.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.h3late.ingestion.exception.InvalidSignatureException;
import com.h3late.ingestion.service.SignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final SignatureService signatureService;
    private final XmlMapper xmlMapper = new XmlMapper();
    public WebhookController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @GetMapping("/youtube")
    public ResponseEntity<String> handleYouTubeVerification(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.challenge") String challenge
    ) {
        if ("subscribe".equals(mode)) {
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.badRequest().body("Invalid mode");
        }
    }


    @PostMapping("/youtube")
    public ResponseEntity<Void> handleYouTubeWebhook(
            @RequestHeader("X-Hub-Signature") String signature,
            @RequestBody String requestBody

    ) {
        if (!signatureService.isValidSignature(signature, requestBody)) {
            throw new InvalidSignatureException("Invalid signature");
        }
        return ResponseEntity.noContent().build();
    }
}
