package com.backend.notes.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.backend.notes.note.SharePermission;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareRequest {
    
    @Email(message = "email must be a well-formed email address")
    @NotBlank(message = "email is required")
    @Schema(
        description = "Email address of user to share note with",
        example = "colleague@example.com",
        format = "email"
    )
    private String email;
    
    @NotNull(message = "permission is required")
    @Schema(
        description = "Share permission level: READ (read-only) or WRITE (read and edit)",
        example = "READ",
        allowableValues = {"READ", "WRITE"}
    )
    private SharePermission permission;
}
