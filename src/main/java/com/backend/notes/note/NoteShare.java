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

@Entity
@Table(name = "note_shares", indexes = {
    @Index(name = "idx_note_shares_shared_with_user_id", columnList = "shared_with_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteShare {
    
    @EmbeddedId
    private NoteShareId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("noteId")
    @JoinColumn(name = "note_id", insertable = false, updatable = false)
    private Note note;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sharedWithUserId")
    @JoinColumn(name = "shared_with_user_id", insertable = false, updatable = false)
    private User sharedWithUser;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SharePermission permission;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime sharedAt;
}
