package com.enspy.syndicmanager.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, length = 2000)
    private String description;
    
    @Column(nullable = false)
    private String location;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @Column(name = "author_name", nullable = false)
    private String authorName;
    
    @Column(name = "author_avatar")
    private String authorAvatar;
    
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;
    
    @ElementCollection
    @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private List<String> images = new ArrayList<>();
    
    @Column(nullable = false)
    private String category;
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;
    
    @Column(name = "notify_members", nullable = false)
    private Boolean notifyMembers = true;
    
    @Column(name = "is_upcoming")
    private Boolean isUpcoming = true;
    
    @ManyToMany
    @JoinTable(
        name = "event_participants",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
