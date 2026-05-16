package com.backend.notes.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.backend.notes.note.SharePermission;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareResponse {
    
    @Schema(description = "Note unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID noteId;
    
    @Schema(description = "User ID that note is shared with", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID sharedWithUserId;
    
    @Schema(description = "Email address of user note is shared with", example = "colleague@example.com")
    private String sharedWithEmail;
    
    @Schema(description = "Share permission level: READ or WRITE", example = "READ")
    private SharePermission permission;
    
    @Schema(description = "Timestamp when share was created", example = "2026-05-15T10:30:00Z")
    private OffsetDateTime sharedAt;
}
