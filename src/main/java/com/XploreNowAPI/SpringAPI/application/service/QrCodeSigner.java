package com.XploreNowAPI.SpringAPI.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Component
public class QrCodeSigner {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    @Value("${checkin.qr.secret:ChangeThisCheckInSecretForProdAtLeast32Chars!}")
    private String secret;

    public String sign(Long scheduleId, Instant expiresAt) {
        String payload = buildPayload(scheduleId, expiresAt.getEpochSecond());
        return encodePayload(payload) + "." + encodeSignature(payload);
    }

    public ParsedQrPayload verify(String qrContent) {
        String[] parts = qrContent == null ? new String[0] : qrContent.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid QR content format");
        }

        String payload = decodeBase64(parts[0]);
        String expectedSignature = encodeSignature(payload);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[1].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Invalid QR signature");
        }

        String[] payloadParts = payload.split("\\.");
        if (payloadParts.length != 2) {
            throw new IllegalArgumentException("Invalid QR payload");
        }

        long scheduleId = Long.parseLong(payloadParts[0]);
        long expiresAtEpochSeconds = Long.parseLong(payloadParts[1]);
        return new ParsedQrPayload(scheduleId, expiresAtEpochSeconds);
    }

    public boolean isExpired(ParsedQrPayload payload) {
        return Instant.now().getEpochSecond() > payload.expiresAtEpochSeconds();
    }

    private String buildPayload(Long scheduleId, long expiresAtEpochSeconds) {
        return scheduleId + "." + expiresAtEpochSeconds;
    }

    private String encodePayload(String payload) {
        return BASE64_URL_ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private String encodeSignature(String payload) {
        return BASE64_URL_ENCODER.encodeToString(signBytes(payload));
    }

    private byte[] signBytes(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign QR content", ex);
        }
    }

    private String decodeBase64(String value) {
        return new String(BASE64_URL_DECODER.decode(value), StandardCharsets.UTF_8);
    }

    public record ParsedQrPayload(Long scheduleId, long expiresAtEpochSeconds) {
    }
}