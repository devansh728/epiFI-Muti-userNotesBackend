package com.backend.notes.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {
    
    @JsonProperty("refresh_token")
    @NotBlank(message = "refresh_token is required")
    @Schema(
        description = "Opaque refresh token obtained from login/register endpoint",
        example = "abc123def456..."
    )
    private String refreshToken;
}
