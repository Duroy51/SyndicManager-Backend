package com.enspy.syndicmanager.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "publications")
public class Publication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 2000)
    private String content;
    
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String image;
    
    @Column(name = "author_name", nullable = false)
    private String authorName;
    
    @Column(name = "author_avatar")
    private String authorAvatar;
    
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relations avec les commentaires et réactions
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "target_id")
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
    
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "target_id")
    @Builder.Default
    private List<Reaction> reactions = new ArrayList<>();
    
    // Compteurs de statistiques (pour optimisation des performances)
    @Column(name = "likes_count")
    private int likesCount;
    
    @Column(name = "comments_count")
    private int commentsCount;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
