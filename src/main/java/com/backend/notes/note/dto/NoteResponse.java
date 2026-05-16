package com.backend.notes.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteResponse {
    
    @Schema(description = "Note unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "Note title", example = "My Important Note")
    private String title;
    
    @Schema(description = "Note content", example = "This is the note content with full-text search support.")
    private String content;
    
    @Schema(description = "Note creation timestamp", example = "2026-05-15T10:30:00Z")
    private OffsetDateTime createdAt;
    
    @Schema(description = "Note last update timestamp", example = "2026-05-15T14:20:00Z")
    private OffsetDateTime updatedAt;
    
    @Schema(description = "Owner user ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID ownerId;
    
    @Schema(description = "Owner email address", example = "owner@example.com")
    private String ownerEmail;
}
