package com.backend.notes.note;

import com.backend.notes.common.dto.PageResponse;
import com.backend.notes.note.dto.NoteResponse;
import com.backend.notes.note.dto.NoteVersionResponse;
import com.backend.notes.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notes/{noteId}/versions")
@RequiredArgsConstructor
@Tag(name = "Versions", description = "View note version history")
@SecurityRequirement(name = "Bearer Authentication")
public class NoteVersionController {

    private final NoteService noteService;

    @GetMapping
    @Operation(summary = "Get note versions", description = "Retrieve version history for a note")
    @ApiResponse(responseCode = "200", description = "Version history retrieved")
    @ApiResponse(responseCode = "404", description = "Note not found or not accessible")
    public ResponseEntity<PageResponse<NoteVersionResponse>> getVersions(
            @PathVariable UUID noteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "editedAt"));
        SecurityUser user = (SecurityUser) authentication.getPrincipal();

        PageResponse<NoteVersionResponse> response = noteService.getNoteVersions(
                noteId,
                user.getUserId(),
                pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{versionId}")
    @Operation(summary = "Get note version", description = "Retrieve a specific version of a note")
    @ApiResponse(responseCode = "200", description = "Version retrieved")
    @ApiResponse(responseCode = "404", description = "Note or version not found")
    public ResponseEntity<NoteVersionResponse> getVersion(
            @PathVariable UUID noteId,
            @PathVariable UUID versionId,
            Authentication authentication) {
        SecurityUser user = (SecurityUser) authentication.getPrincipal();

        NoteVersionResponse response = noteService.getNoteVersion(
                noteId,
                versionId,
                user.getUserId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{versionId}/restore")
    @Operation(summary = "Restore note version")
    @ApiResponse(responseCode = "200", description = "Version restored")
    public ResponseEntity<NoteResponse> restoreVersion(
            @PathVariable UUID noteId,
            @PathVariable UUID versionId,
            Authentication authentication) {
        SecurityUser user = (SecurityUser) authentication.getPrincipal();

        NoteResponse response = noteService.restoreVersion(
                noteId,
                versionId,
                user.getUserId());

        return ResponseEntity.ok(response);
    }
}
