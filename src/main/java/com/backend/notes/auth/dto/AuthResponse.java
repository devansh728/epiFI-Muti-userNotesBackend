package com.backend.notes.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    @JsonProperty("access_token")
    @Schema(
        description = "JWT access token (15-minute TTL, stateless, HS256-signed)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;
    
    @JsonProperty("refresh_token")
    @Schema(
        description = "Opaque refresh token (7-day TTL, SHA-256 hashed at rest). Used to get new access tokens. Rotated on refresh.",
        example = "abc123def456..."
    )
    private String refreshToken;
    
    @JsonProperty("expires_in")
    @Schema(
        description = "Access token expiration time in seconds",
        example = "900"
    )
    private Long expiresIn;
}
