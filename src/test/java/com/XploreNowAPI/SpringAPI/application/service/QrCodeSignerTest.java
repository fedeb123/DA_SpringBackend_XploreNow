package com.XploreNowAPI.SpringAPI.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QrCodeSignerTest {

    @Test
    void signAndVerify_ReturnsOriginalScheduleAndExpiration() {
        QrCodeSigner signer = newSigner();
        Instant expiresAt = Instant.now().plusSeconds(3600);

        String qrContent = signer.sign(15L, expiresAt);
        QrCodeSigner.ParsedQrPayload payload = signer.verify(qrContent);

        assertEquals(15L, payload.scheduleId());
        assertEquals(expiresAt.getEpochSecond(), payload.expiresAtEpochSeconds());
        assertFalse(signer.isExpired(payload));
    }

    @Test
    void verify_WhenSignatureIsTampered_ThrowsIllegalArgumentException() {
        QrCodeSigner signer = newSigner();
        String qrContent = signer.sign(15L, Instant.now().plusSeconds(3600));
        String tampered = qrContent.substring(0, qrContent.length() - 1) + "A";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> signer.verify(tampered));
        assertEquals("Invalid QR signature", exception.getMessage());
    }

    @Test
    void isExpired_WhenExpirationIsPast_ReturnsTrue() {
        QrCodeSigner signer = newSigner();
        QrCodeSigner.ParsedQrPayload payload = new QrCodeSigner.ParsedQrPayload(15L, Instant.now().minusSeconds(60).getEpochSecond());

        assertTrue(signer.isExpired(payload));
    }

    private QrCodeSigner newSigner() {
        QrCodeSigner signer = new QrCodeSigner();
        ReflectionTestUtils.setField(signer, "secret", "test-secret-32-chars-minimum!!!!");
        return signer;
    }
}