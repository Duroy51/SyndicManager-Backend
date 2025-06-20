package com.enspy.syndicmanager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicationRequest {
    private String content;
    private String image;
    private String authorName;
    private String authorAvatar;
    private UUID authorId;
}
