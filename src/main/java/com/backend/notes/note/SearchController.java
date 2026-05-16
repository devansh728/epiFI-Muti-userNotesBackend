package com.backend.notes.note;

import com.backend.notes.common.dto.PageResponse;
import com.backend.notes.note.dto.NoteResponse;
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
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Full-text search on notes")
@SecurityRequirement(name = "Bearer Authentication")
public class SearchController {
    
    private final NoteService noteService;
    
    @GetMapping
    @Operation(summary = "Search notes", description = "Full-text search on notes accessible to user")
    @ApiResponse(responseCode = "200", description = "Search results")
    @ApiResponse(responseCode = "400", description = "Invalid search query")
    public ResponseEntity<PageResponse<NoteResponse>> search(
        @RequestParam(required = true) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Authentication authentication
    ) {
        // Validate query length
        if (q == null || q.trim().length() < 1 || q.length() > 200) {
            throw new IllegalArgumentException("Search query must be between 1 and 200 characters");
        }
        
        size = Math.min(size, 100); // Cap size at 100
        Pageable pageable = PageRequest.of(page, size);
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        
        PageResponse<NoteResponse> response = noteService.searchNotes(
            user.getUserId(),
            q.trim(),
            pageable
        );
        
        return ResponseEntity.ok(response);
    }
}
