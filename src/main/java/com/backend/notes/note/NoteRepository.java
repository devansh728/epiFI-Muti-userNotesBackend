package com.backend.notes.note;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    @EntityGraph(attributePaths = "owner")
    Page<Note> findByOwnerIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            UUID ownerId, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    Optional<Note> findByIdAndDeletedAtIsNull(UUID id);

    @Query(value = """
            SELECT *
            FROM notes n
            WHERE (
                n.owner_id = :userId
                OR EXISTS (
                    SELECT 1 FROM note_shares ns
                    WHERE ns.note_id = n.id
                    AND ns.shared_with_user_id = :userId
                )
            )
            AND n.deleted_at IS NULL
            AND n.search_vector @@ plainto_tsquery('english', :query)
            ORDER BY ts_rank_cd(
                n.search_vector,
                plainto_tsquery('english', :query)
            ) DESC
            """, countQuery = """
            SELECT COUNT(*)
            FROM notes n
            WHERE (
                n.owner_id = :userId
                OR EXISTS (
                    SELECT 1 FROM note_shares ns
                    WHERE ns.note_id = n.id
                    AND ns.shared_with_user_id = :userId
                )
            )
            AND n.deleted_at IS NULL
            AND n.search_vector @@ plainto_tsquery('english', :query)
            """, nativeQuery = true)
    Page<Note> searchNotesByQuery(
            @Param("userId") UUID userId,
            @Param("query") String query,
            Pageable pageable);

    @Query("""
        SELECT n FROM Note n
        WHERE n.id = :noteId
        AND n.deletedAt IS NULL
        AND (
            n.owner.id = :userId
            OR EXISTS (
                SELECT 1 FROM NoteShare ns
                WHERE ns.id.noteId = n.id
                AND ns.id.sharedWithUserId = :userId
            )
        )
        """)
    Optional<Note> findByIdAndAccessibleByUser(
            @Param("noteId") UUID noteId,
            @Param("userId") UUID userId);

    @Query("""
        SELECT n FROM Note n
        WHERE n.id = :noteId
        AND n.deletedAt IS NULL
        AND (
            n.owner.id = :userId
            OR EXISTS (
                SELECT 1 FROM NoteShare ns
                WHERE ns.id.noteId = n.id
                AND ns.id.sharedWithUserId = :userId
                AND ns.permission = com.backend.notes.note.SharePermission.WRITE
            )
        )
        """)
    Optional<Note> findByIdAndEditableByUser(
            @Param("noteId") UUID noteId,
            @Param("userId") UUID userId);

    List<Note> findByIdIn(List<UUID> ids);
}
