package com.enspy.syndicmanager.dto.request;

import com.enspy.syndicmanager.models.Reaction.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReactionRequest {
    private UUID publicationId; // ID de la publication concernée
    private UUID userId;
    private ReactionType reactionType;
}
