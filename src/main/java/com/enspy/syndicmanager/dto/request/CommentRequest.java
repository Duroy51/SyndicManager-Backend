package com.enspy.syndicmanager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {
    private String content;
    private UUID publicationId; // ID de la publication commentée
    private UUID userId;
    private UUID parentId; // Null pour les commentaires de premier niveau
}
