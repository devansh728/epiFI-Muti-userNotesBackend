package com.backend.notes.about;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Meta", description = "Application information")
public class AboutController {
    
    @GetMapping("/about")
    @Operation(summary = "About", description = "Get application information and available features")
    @ApiResponse(responseCode = "200", description = "Application information")
    public ResponseEntity<Map<String, Object>> about() {
        Map<String, Object> response = Map.ofEntries(
            Map.entry("name", "Notes App"),
            Map.entry("version", "1.0.0"),
            Map.entry("description", "Multi-user Notes service with sharing, full-text search, and version history"),
            Map.entry("email", "contact@notes-app.example.com"),
            Map.entry("my_features", Arrays.asList(
                Map.ofEntries(
                    Map.entry("name", "Share Permissions"),
                    Map.entry("description", "Share notes with granular permissions (READ/WRITE). READ-only collaborators can view, WRITE collaborators can edit. Only the owner can delete or re-share.")
                ),
                Map.ofEntries(
                    Map.entry("name", "Note Version History"),
                    Map.entry("description", "Every note edit creates a version snapshot. Retrieve version history via GET /notes/{id}/versions (paginated). Inspect individual versions via GET /notes/{id}/versions/{versionId}. Last 50 versions are kept per note.")
                )
            ))
        );
        
        return ResponseEntity.ok(response);
    }
}
