package com.h3late.ingestion.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class SignatureService {

    private final String secret;

    public SignatureService(@Value("${youtube.webhook.secret}") String secret) {
        this.secret = secret;
    }
    private static final String HMAC_ALGORITHM = "HmacSHA1";

    public boolean isValidSignature(String signatureHeader, String payload) {

        if (signatureHeader == null || !signatureHeader.startsWith("sha1=")) {
            return false;
        }

        try {
            // 1. Extract the signature from the header
            String providedHex = signatureHeader.substring(5);

            // 2. Compute HMAC-SHA1 of the payload using the secret
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );

            // 3. Compute the HMAC
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // 4. Convert to Hex String
            String calculatedHex = HexFormat.of().formatHex(rawHmac);

            // 5. Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(
                    providedHex.getBytes(StandardCharsets.UTF_8),
                    calculatedHex.getBytes(StandardCharsets.UTF_8)
            );

        } catch (Exception E) {
            return false;
        }
    }
}
