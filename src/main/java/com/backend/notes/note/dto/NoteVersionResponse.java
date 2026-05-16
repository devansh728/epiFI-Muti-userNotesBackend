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
public class NoteVersionResponse {
    
    @Schema(description = "Version record unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "Note title at time of this version", example = "My Important Note")
    private String title;
    
    @Schema(description = "Note content at time of this version", example = "This is the note content as it was at version time.")
    private String content;
    
    @Schema(description = "User ID that created this version", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID editedByUserId;
    
    @Schema(description = "Email of user that created this version", example = "editor@example.com")
    private String editedByEmail;
    
    @Schema(description = "When this version was created", example = "2026-05-15T14:20:00Z")
    private OffsetDateTime editedAt;

    @Schema(description = "Version number (incremental)", example = "3")
    private Integer versionNumber;

    @Schema(description = "Note unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID noteId;
}
