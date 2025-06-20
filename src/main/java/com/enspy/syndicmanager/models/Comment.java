package com.enspy.syndicmanager.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    // ID de la publication à laquelle ce commentaire est associé
    @Column(name = "publication_id", nullable = false)
    private UUID publicationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Pour les réponses aux commentaires
    @Column(name = "parent_id")
    private UUID parentId;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
