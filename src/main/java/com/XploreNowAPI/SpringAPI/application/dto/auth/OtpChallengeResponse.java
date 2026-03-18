package com.XploreNowAPI.SpringAPI.application.dto.auth;

public record OtpChallengeResponse(
        String email,
        String purpose,
        long expiresInSeconds,
        String message
) {
}
