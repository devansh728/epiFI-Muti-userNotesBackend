package com.backend.notes.note;

import com.backend.notes.note.dto.ShareRequest;
import com.backend.notes.note.dto.ShareResponse;
import com.backend.notes.security.SecurityUser;
import com.backend.notes.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notes/{noteId}/share")
@RequiredArgsConstructor
@Tag(name = "Sharing", description = "Share notes with other users")
@SecurityRequirement(name = "Bearer Authentication")
public class ShareController {
    
    private final NoteService noteService;
    private final UserRepository userRepository;
    
    @PostMapping
    @Operation(summary = "Share note", description = "Share a note with another user (owner only)")
    @ApiResponse(responseCode = "200", description = "Note shared successfully")
    @ApiResponse(responseCode = "400", description = "Validation error or cannot share with self")
    @ApiResponse(responseCode = "403", description = "Permission denied (not owner)")
    @ApiResponse(responseCode = "404", description = "Note or user not found")
    public ResponseEntity<ShareResponse> shareNote(
        @PathVariable UUID noteId,
        @Valid @RequestBody ShareRequest request,
        Authentication authentication
    ) {
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        
        // Find the user to share with
        UUID sharedWithUserId = userRepository.findByEmailIgnoreCase(request.getEmail())
            .map(u -> u.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        ShareResponse response = noteService.shareNote(
            noteId,
            user.getUserId(),
            sharedWithUserId,
            request.getPermission()
        );
        
        return ResponseEntity.ok(response);
    }
}
