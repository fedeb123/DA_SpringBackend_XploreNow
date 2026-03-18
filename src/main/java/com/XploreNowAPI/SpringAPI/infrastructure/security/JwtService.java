package com.XploreNowAPI.SpringAPI.infrastructure.security;

import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public JwtService(
            @Value("${xplorenow.jwt.secret}") String secret,
            @Value("${xplorenow.jwt.expiration-minutes:120}") long expirationMinutes
    ) {
        this.signingKey = Keys.hmacShaKeyFor(ensureMinSecret(secret).getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .claims(Map.of(
                        "uid", user.getId(),
                        "email", user.getEmail()
                ))
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        String username = extractUsername(token);
        return expectedUsername.equalsIgnoreCase(username) && !isTokenExpired(token);
    }

    public long getExpirationSeconds() {
        return expirationMinutes * 60;
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claimResolver.apply(claims);
    }

    private static String ensureMinSecret(String secret) {
        if (secret == null) {
            return "fallback-secret-key-should-be-overridden-123456";
        }

        String trimmed = secret.trim();
        if (trimmed.length() >= 32) {
            return trimmed;
        }

        StringBuilder sb = new StringBuilder(trimmed);
        while (sb.length() < 32) {
            sb.append('0');
        }
        return sb.toString();
    }
}
