package com.backend.notes.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteRequest {
    
    @NotBlank(message = "title is required")
    @Size(min = 1, max = 200, message = "title must be between 1 and 200 characters")
    @Schema(
        description = "Note title",
        example = "My Important Note",
        minLength = 1,
        maxLength = 200
    )
    private String title;
    
    @Size(max = 100000, message = "content must be at most 100000 characters")
    @Schema(
        description = "Note content (max 100,000 characters)",
        example = "This is the note content with full-text search support.",
        maxLength = 100000
    )
    private String content;
}
