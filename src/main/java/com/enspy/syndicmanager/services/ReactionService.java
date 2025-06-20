package com.enspy.syndicmanager.services;

import com.enspy.syndicmanager.dto.request.ReactionRequest;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.models.Reaction;
import com.enspy.syndicmanager.repositories.ReactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final ObjectMapper objectMapper;

    public ResponseDto getAllReactions() {
        try {
            List<Reaction> reactions = reactionRepository.findAll();
            String jsonData = objectMapper.writeValueAsString(reactions);
            return ResponseDto.builder()
                    .status(HttpStatus.OK.value())
                    .text("Reactions retrieved successfully")
                    .data(jsonData)
                    .build();
        } catch (JsonProcessingException e) {
            return ResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .text("Failed to retrieve reactions: " + e.getMessage())
                    .build();
        }
    }

    public ResponseDto getReactionsByPublicationId(UUID publicationId) {
        try {
            List<Reaction> reactions = reactionRepository.findByPublicationId(publicationId);
            String jsonData = objectMapper.writeValueAsString(reactions);
            return ResponseDto.builder()
                    .status(HttpStatus.OK.value())
                    .text("Reactions for target retrieved successfully")
                    .data(jsonData)
                    .build();
        } catch (JsonProcessingException e) {
            return ResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .text("Failed to retrieve reactions: " + e.getMessage())
                    .build();
        }
    }

    public ResponseDto createReaction(ReactionRequest request) {
        try {
            // Vérifier si la réaction existe déjà
            Optional<Reaction> existingReaction = reactionRepository.findByUserIdAndPublicationIdAndReactionType(
                    request.getUserId(),
                    request.getPublicationId(),
                    request.getReactionType()
            );
            
            if (existingReaction.isPresent()) {
                String jsonData = objectMapper.writeValueAsString(existingReaction.get());
                return ResponseDto.builder()
                        .status(HttpStatus.OK.value())
                        .text("Reaction already exists")
                        .data(jsonData)
                        .build();
            }
            
            Reaction reaction = Reaction.builder()
                    .publicationId(request.getPublicationId())
                    .userId(request.getUserId())
                    .reactionType(request.getReactionType())
                    .build();
            
            Reaction savedReaction = reactionRepository.save(reaction);
            String jsonData = objectMapper.writeValueAsString(savedReaction);
            
            return ResponseDto.builder()
                    .status(HttpStatus.CREATED.value())
                    .text("Reaction created successfully")
                    .data(jsonData)
                    .build();
        } catch (JsonProcessingException e) {
            return ResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .text("Failed to create reaction: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ResponseDto removeReaction(UUID userId, UUID publicationId, Reaction.ReactionType reactionType) {
        try {
            Optional<Reaction> reaction = reactionRepository.findByUserIdAndPublicationIdAndReactionType(userId, publicationId, reactionType);
            if (reaction.isEmpty()) {
                return ResponseDto.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .text("Reaction not found")
                        .build();
            }
            
            reactionRepository.deleteByUserIdAndPublicationIdAndReactionType(userId, publicationId, reactionType);
            
            return ResponseDto.builder()
                    .status(HttpStatus.OK.value())
                    .text("Reaction removed successfully")
                    .build();
        } catch (Exception e) {
            return ResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .text("Failed to remove reaction: " + e.getMessage())
                    .build();
        }
    }
}
