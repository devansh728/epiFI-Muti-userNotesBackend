package com.backend.notes.note;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import com.backend.notes.user.User;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "note_versions", indexes = {
        @Index(name = "idx_note_versions_note_id", columnList = "note_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by_user_id")
    private User editedByUser;

    @Column(nullable = false)
    private Integer versionNumber;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime editedAt;
}
