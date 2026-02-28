package com.h3late.ingestion;

import com.h3late.ingestion.service.SignatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignatureServiceTest {
    private SignatureService signatureService;
    private final String secret = "test-secret";

    @BeforeEach
    void setUp() {
        // Pass your secret manually since we aren't loading the full Spring app
        signatureService = new SignatureService(secret);
    }

    @Test
    void shouldReturnTrue_WhenSignatureIsValid() {
        String payload = "{\"test\": \"data\"}";
        // You'll need to generate a valid HMAC-SHA1 for this payload + secret
        String validSignature = "sha1=c2ccc0cce1e945cbf25dc76e30283f7de226883b";
        boolean result = signatureService.isValidSignature(validSignature, payload);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_WhenPayloadIsTampered() {
        String payload = "original data"; // used to generate signature below
        String tamperedPayload = "hacker data";
        String signature = "sha1=b2fb19859b19cdfcc56d1f22347322aaaf07b64c";

        boolean result = signatureService.isValidSignature(signature, tamperedPayload);

        assertFalse(result);
    }
}