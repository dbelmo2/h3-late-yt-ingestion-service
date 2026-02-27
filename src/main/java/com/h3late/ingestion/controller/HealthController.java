package com.h3late.ingestion.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.time.Instant;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> getHealth() {
        return Map.of(
                "status", "ok",
                "timestamp", Instant.now().toString(),
                "service", "ingestion-service"
        );
    }
}