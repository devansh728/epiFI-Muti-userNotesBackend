package com.backend.notes.auth;

import com.backend.notes.auth.dto.AuthResponse;
import com.backend.notes.security.JwtService;
import com.backend.notes.user.User;
import com.backend.notes.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${app.jwt.refresh-token-ttl}")
    private Duration refreshTokenTtl;
    
    /**
     * Authenticate user and return tokens
     */
    public AuthResponse login(String email, String password) {
        User user = userService.findByEmail(email);
        
        if (!userService.verifyPassword(user, password)) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        // Revoke all previous tokens (except current one)
        refreshTokenRepository.revokeAllTokensByUserId(user.getId());
        
        // Generate new tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken();
        String refreshTokenHash = jwtService.hashToken(refreshToken);
        
        // Save refresh token
        RefreshToken tokenEntity = RefreshToken.builder()
            .user(user)
            .tokenHash(refreshTokenHash)
            .expiresAt(OffsetDateTime.now().plus(refreshTokenTtl))
            .build();
        refreshTokenRepository.save(tokenEntity);
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(900L) // 15 minutes in seconds
            .build();
    }
    
    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshAccessToken(String refreshToken) {
        String tokenHash = jwtService.hashToken(refreshToken);
        
        RefreshToken tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        
        if (tokenEntity.getRevokedAt() != null) {
            // Token was revoked - mark reuse detected
            log.warn("Reuse of revoked refresh token detected for user: {}", tokenEntity.getUser().getId());
            refreshTokenRepository.revokeAllTokensByUserId(tokenEntity.getUser().getId());
            throw new IllegalArgumentException("Refresh token has been revoked");
        }
        
        if (OffsetDateTime.now().isAfter(tokenEntity.getExpiresAt())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }
        
        User user = tokenEntity.getUser();
        
        // Revoke old token
        refreshTokenRepository.revokeByTokenHash(tokenHash);
        
        // Generate new tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken();
        String newRefreshTokenHash = jwtService.hashToken(newRefreshToken);
        
        // Save new refresh token
        RefreshToken newTokenEntity = RefreshToken.builder()
            .user(user)
            .tokenHash(newRefreshTokenHash)
            .expiresAt(OffsetDateTime.now().plus(refreshTokenTtl))
            .build();
        refreshTokenRepository.save(newTokenEntity);
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(newRefreshToken)
            .expiresIn(900L)
            .build();
    }
    
    /**
     * Logout by revoking refresh token
     */
    public void logout(String refreshToken) {
        try {
            String tokenHash = jwtService.hashToken(refreshToken);
            refreshTokenRepository.revokeByTokenHash(tokenHash);
        } catch (Exception e) {
            log.debug("Error revoking refresh token: {}", e.getMessage());
        }
    }
}
