package com.backend.notes.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    
    @Email(message = "email must be a well-formed email address")
    @Size(max = 254, message = "email must be at most 254 characters")
    @NotBlank(message = "email is required")
    @Schema(
        description = "User email address",
        example = "user@example.com",
        format = "email"
    )
    private String email;
    
    @NotBlank(message = "password is required")
    @Size(min = 8, max = 128, message = "password must be between 8 and 128 characters")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d).*$",
        message = "password must contain at least one letter and one digit"
    )
    @Schema(
        description = "Password (8-128 chars, at least one letter and one digit)",
        example = "MySecurePassword123"
    )
    private String password;
}
