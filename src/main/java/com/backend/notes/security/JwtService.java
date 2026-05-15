package com.backend.notes.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {
    
    private final SecretKey key;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;
    
    public JwtService(
        @Value("${app.jwt.secret}") String jwtSecret,
        @Value("${app.jwt.access-token-ttl}") Duration accessTokenTtl,
        @Value("${app.jwt.refresh-token-ttl}") Duration refreshTokenTtl
    ) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }
    
    /**
     * Generate a JWT access token
     */
    public String generateAccessToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTokenTtl);
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(key)
            .compact();
    }
    
    /**
     * Generate a refresh token (opaque token string, not a JWT)
     * The actual token will be hashed and stored; this returns the plaintext
     */
    public String generateRefreshToken() {
        // Return a 256-bit random token (base64 encoded)
        byte[] randomBytes = new byte[32];
        java.util.Random random = new java.util.Random();
        random.nextBytes(randomBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * Extract user ID from JWT
     */
    public UUID extractUserId(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }
    
    /**
     * Extract email from JWT
     */
    public String extractEmail(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }
    
    /**
     * Validate JWT token
     */
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Parse and validate JWT token
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    /**
     * Hash a refresh token using SHA-256
     */
    public String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
