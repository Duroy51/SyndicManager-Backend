package com.enspy.syndicmanager.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ID de la publication à laquelle cette réaction est associée
    @Column(name = "publication_id", nullable = false)
    private UUID publicationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "reaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ReactionType {
        LIKE, LOVE, LAUGH, WOW, SAD, ANGRY
    }
}
