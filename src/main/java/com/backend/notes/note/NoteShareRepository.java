package com.backend.notes.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteShareRepository extends JpaRepository<NoteShare, NoteShareId> {
    
    @Query("SELECT ns FROM NoteShare ns WHERE ns.id.noteId = :noteId AND ns.id.sharedWithUserId = :userId")
    Optional<NoteShare> findByNoteIdAndUserId(
        @Param("noteId") UUID noteId, 
        @Param("userId") UUID userId);
    
    @Query("SELECT ns FROM NoteShare ns WHERE ns.id.noteId = :noteId")
    List<NoteShare> findAllByNoteId(@Param("noteId") UUID noteId);
    
    @Query("SELECT ns FROM NoteShare ns WHERE ns.id.sharedWithUserId = :userId")
    List<NoteShare> findAllBySharedWithUserId(@Param("userId") UUID userId);
    
    boolean existsByIdNoteIdAndIdSharedWithUserId(UUID noteId, UUID sharedWithUserId);
    
    void deleteByIdNoteIdAndIdSharedWithUserId(UUID noteId, UUID sharedWithUserId);
}
