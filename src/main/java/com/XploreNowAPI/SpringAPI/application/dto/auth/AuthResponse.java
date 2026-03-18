package com.XploreNowAPI.SpringAPI.application.dto.auth;

public record AuthResponse(
        String token,
        long expiresInSeconds,
        String tokenType,
        String email,
        String fullName
) {
}
