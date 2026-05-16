package com.backend.notes.note;

import com.backend.notes.common.dto.PageResponse;
import com.backend.notes.note.dto.NoteRequest;
import com.backend.notes.note.dto.NoteResponse;
import com.backend.notes.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Create, read, update, and delete notes")
@SecurityRequirement(name = "Bearer Authentication")
public class NoteController {
    
    private final NoteService noteService;
    private static final Set<String> ALLOWED_SORT_FIELDS = new HashSet<>(
        Arrays.asList("createdAt", "updatedAt", "title")
    );
    
    @GetMapping
    @Operation(summary = "List notes", description = "Get all notes of the authenticated user (paginated and sortable)")
    @ApiResponse(responseCode = "200", description = "Notes retrieved successfully")
    public ResponseEntity<PageResponse<NoteResponse>> listNotes(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort,
        Authentication authentication
    ) {
        size = Math.min(size, 100); // Cap size at 100
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        
        // Validate sort field to prevent SQL injection
        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            sortField = "createdAt";
        }
        
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        
        PageResponse<NoteResponse> response = noteService.getUserNotes(user.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get note", description = "Retrieve a note by ID if accessible")
    @ApiResponse(responseCode = "200", description = "Note retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Note not found or not accessible")
    public ResponseEntity<NoteResponse> getNote(
        @PathVariable UUID id,
        Authentication authentication
    ) {
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        NoteResponse response = noteService.getNote(id, user.getUserId());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Create note", description = "Create a new note")
    @ApiResponse(responseCode = "201", description = "Note created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<NoteResponse> createNote(
        @Valid @RequestBody NoteRequest request,
        Authentication authentication
    ) {
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        NoteResponse response = noteService.createNote(user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update note", description = "Update a note if owner or has WRITE share")
    @ApiResponse(responseCode = "200", description = "Note updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Permission denied")
    @ApiResponse(responseCode = "404", description = "Note not found")
    @ApiResponse(responseCode = "409", description = "Concurrent modification detected")
    public ResponseEntity<NoteResponse> updateNote(
        @PathVariable UUID id,
        @Valid @RequestBody NoteRequest request,
        Authentication authentication
    ) {
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        NoteResponse response = noteService.updateNote(id, user.getUserId(), request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete note", description = "Delete a note (soft delete, owner only)")
    @ApiResponse(responseCode = "204", description = "Note deleted successfully")
    @ApiResponse(responseCode = "403", description = "Permission denied")
    @ApiResponse(responseCode = "404", description = "Note not found")
    public ResponseEntity<Void> deleteNote(
        @PathVariable UUID id,
        Authentication authentication
    ) {
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        noteService.deleteNote(id, user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
