package com.backend.notes.note;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteVersionRepository extends JpaRepository<NoteVersion, UUID> {
    
    @Query("SELECT nv FROM NoteVersion nv WHERE nv.note.id = :noteId ORDER BY nv.editedAt ASC")
    Page<NoteVersion> findByNoteIdOrderByEditedAtAsc(
        @Param("noteId") UUID noteId, 
        Pageable pageable);
    
    @Query("SELECT nv FROM NoteVersion nv WHERE nv.id = :versionId AND nv.note.id = :noteId")
    Optional<NoteVersion> findByIdAndNoteId(
        @Param("versionId") UUID versionId,
        @Param("noteId") UUID noteId);
    
    @Query("SELECT COUNT(nv) FROM NoteVersion nv WHERE nv.note.id = :noteId")
    long countByNoteId(@Param("noteId") UUID noteId);
}
