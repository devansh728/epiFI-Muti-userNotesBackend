package com.backend.notes.note;

import com.backend.notes.common.dto.PageResponse;
import com.backend.notes.note.dto.NoteRequest;
import com.backend.notes.note.dto.NoteResponse;
import com.backend.notes.note.dto.NoteVersionResponse;
import com.backend.notes.note.dto.ShareResponse;
import com.backend.notes.user.User;
import com.backend.notes.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoteService {
    
    private final NoteRepository noteRepository;
    private final NoteShareRepository noteShareRepository;
    private final NoteVersionRepository noteVersionRepository;
    private final UserRepository userRepository;
    
    /**
     * Get user's notes (paginated)
     */
    public PageResponse<NoteResponse> getUserNotes(UUID userId, Pageable pageable) {
        Page<Note> page = noteRepository.findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable);
        List<NoteResponse> content = page.getContent().stream()
            .map(this::toNoteResponse)
            .collect(Collectors.toList());
        
        return PageResponse.<NoteResponse>builder()
            .content(content)
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }
    
    /**
     * Get a note by ID if accessible by user
     */
    @Cacheable(value = "notes", key = "#noteId")
    public NoteResponse getNote(UUID noteId, UUID userId) {
        Note note = noteRepository.findByIdAndAccessibleByUser(noteId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found or not accessible"));
        return toNoteResponse(note);
    }
    
    /**
     * Create a new note
     */
    @CacheEvict(value = "userNotesPage", key = "#userId")
    public NoteResponse createNote(UUID userId, NoteRequest request) {
        User owner = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Note note = Note.builder()
            .owner(owner)
            .title(request.getTitle().trim())
            .content(request.getContent() != null ? request.getContent() : "")
            .version(0)
            .build();
        
        Note saved = noteRepository.save(note);
        return toNoteResponse(saved);
    }
    
    /**
     * Update a note (editable by owner or WRITE-share holder)
     */
    @CacheEvict(value = {"notes", "userNotesPage"}, key = "#userId")
    public NoteResponse updateNote(UUID noteId, UUID userId, NoteRequest request) {
        Note note = noteRepository.findByIdAndEditableByUser(noteId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found or not editable"));
        
        // Create version snapshot before updating
        NoteVersion version = NoteVersion.builder()
            .note(note)
            .title(note.getTitle())
            .content(note.getContent())
            .editedByUser(userRepository.findById(userId).orElse(null))
            .build();
        noteVersionRepository.save(version);
        
        // Update note
        note.setTitle(request.getTitle().trim());
        note.setContent(request.getContent() != null ? request.getContent() : "");
        
        Note updated = noteRepository.save(note);
        
        // Prune old versions (keep last 50)
        long versionCount = noteVersionRepository.countByNoteId(noteId);
        if (versionCount > 50) {
            long toDelete = versionCount - 50;
            List<NoteVersion> oldVersions = noteVersionRepository.findByNoteIdOrderByEditedAtDesc(noteId, Pageable.ofSize((int) toDelete))
                .getContent();
            noteVersionRepository.deleteAll(oldVersions);
        }
        
        return toNoteResponse(updated);
    }
    
    /**
     * Delete a note (soft delete, owner only)
     */
    @CacheEvict(value = {"notes", "userNotesPage"}, key = "#userId")
    public void deleteNote(UUID noteId, UUID userId) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        
        // Only owner can delete
        if (!note.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Only owner can delete note");
        }
        
        // Soft delete
        note.setDeletedAt(java.time.OffsetDateTime.now());
        noteRepository.save(note);
    }
    
    /**
     * Share note with another user
     */
    @CacheEvict(value = "notes", key = "#noteId")
    public ShareResponse shareNote(UUID noteId, UUID userId, UUID sharedWithUserId, SharePermission permission) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        
        // Only owner can share
        if (!note.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Only owner can share note");
        }
        
        // Cannot share with self
        if (userId.equals(sharedWithUserId)) {
            throw new IllegalArgumentException("Cannot share note with yourself");
        }
        
        User sharedWithUser = userRepository.findById(sharedWithUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Upsert: update if exists, create if not
        NoteShareId shareId = new NoteShareId(noteId, sharedWithUserId);
        NoteShare share = noteShareRepository.findByNoteIdAndUserId(noteId, sharedWithUserId)
            .orElse(NoteShare.builder()
                .id(shareId)
                .note(note)
                .sharedWithUser(sharedWithUser)
                .build());
        
        share.setPermission(permission);
        NoteShare saved = noteShareRepository.save(share);
        
        return toShareResponse(saved);
    }
    
    /**
     * Unshare note
     */
    @CacheEvict(value = "notes", key = "#noteId")
    public void unshareNote(UUID noteId, UUID userId, UUID sharedWithUserId) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        
        // Only owner can unshare
        if (!note.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Only owner can unshare note");
        }
        
        noteShareRepository.deleteByIdNoteIdAndIdSharedWithUserId(noteId, sharedWithUserId);
    }
    
    /**
     * Search notes accessible by user
     */
    public PageResponse<NoteResponse> searchNotes(UUID userId, String query, Pageable pageable) {
        Page<Note> results = noteRepository.searchNotesByQuery(userId, query, pageable);
        
        List<NoteResponse> content = results.getContent().stream()
            .map(this::toNoteResponse)
            .toList();
        
        return PageResponse.<NoteResponse>builder()
            .content(content)
            .pageNumber(results.getNumber())
            .pageSize(results.getSize())
            .totalElements(results.getTotalElements())
            .totalPages(results.getTotalPages())
            .last(results.isLast())
            .build();
    }
    
    /**
     * Get note versions
     */
    public PageResponse<NoteVersionResponse> getNoteVersions(UUID noteId, UUID userId, Pageable pageable) {
        // Verify user has access to note
        noteRepository.findByIdAndAccessibleByUser(noteId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found or not accessible"));
        
        Page<NoteVersion> versions = noteVersionRepository.findByNoteIdOrderByEditedAtDesc(noteId, pageable);
        List<NoteVersionResponse> content = versions.getContent().stream()
            .map(this::toNoteVersionResponse)
            .collect(Collectors.toList());
        
        return PageResponse.<NoteVersionResponse>builder()
            .content(content)
            .pageNumber(versions.getNumber())
            .pageSize(versions.getSize())
            .totalElements(versions.getTotalElements())
            .totalPages(versions.getTotalPages())
            .last(versions.isLast())
            .build();
    }
    
    /**
     * Get specific note version
     */
    public NoteVersionResponse getNoteVersion(UUID noteId, UUID versionId, UUID userId) {
        // Verify user has access
        noteRepository.findByIdAndAccessibleByUser(noteId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Note not found or not accessible"));
        
        NoteVersion version = noteVersionRepository.findByIdAndNoteId(versionId, noteId)
            .orElseThrow(() -> new IllegalArgumentException("Version not found"));
        
        return toNoteVersionResponse(version);
    }
    
    // Helper methods for conversion
    
    private NoteResponse toNoteResponse(Note note) {
        return NoteResponse.builder()
            .id(note.getId())
            .title(note.getTitle())
            .content(note.getContent())
            .createdAt(note.getCreatedAt())
            .updatedAt(note.getUpdatedAt())
            .ownerId(note.getOwner().getId())
            .ownerEmail(note.getOwner().getEmail())
            .build();
    }
    
    private ShareResponse toShareResponse(NoteShare share) {
        return ShareResponse.builder()
            .noteId(share.getId().getNoteId())
            .sharedWithUserId(share.getId().getSharedWithUserId())
            .sharedWithEmail(share.getSharedWithUser().getEmail())
            .permission(share.getPermission())
            .sharedAt(share.getSharedAt())
            .build();
    }
    
    private NoteVersionResponse toNoteVersionResponse(NoteVersion version) {
        return NoteVersionResponse.builder()
            .id(version.getId())
            .title(version.getTitle())
            .content(version.getContent())
            .editedByUserId(version.getEditedByUser() != null ? version.getEditedByUser().getId() : null)
            .editedByEmail(version.getEditedByUser() != null ? version.getEditedByUser().getEmail() : null)
            .editedAt(version.getEditedAt())
            .build();
    }
}
