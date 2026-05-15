package com.backend.notes.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    
    @Email(message = "email must be a well-formed email address")
    @NotBlank(message = "email is required")
    @Schema(
        description = "User email address",
        example = "user@example.com",
        format = "email"
    )
    private String email;
    
    @NotBlank(message = "password is required")
    @Schema(
        description = "User password",
        example = "MySecurePassword123"
    )
    private String password;
}
