package com.enspy.syndicmanager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {
    private String title;
    private String description;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String authorName;
    private String authorAvatar;
    private UUID authorId;
    private List<String> images;
    private String category;
    private Boolean isPublic;
    private Boolean notifyMembers;
}
