package com.enspy.syndicmanager.services;

import com.enspy.syndicmanager.dto.request.CommentRequest;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.models.Comment;
import com.enspy.syndicmanager.repositories.CommentRepository;
import com.enspy.syndicmanager.repositories.PublicationRepository;
import com.enspy.syndicmanager.repositories.UserRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final PublicationRepository publicationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ResponseDto getAllComments() {
        try {
            List<Comment> comments = commentRepository.findAll();
            String jsonData = objectMapper.writeValueAsString(comments);
            return ResponseDto.builder()
                    .status(HttpStatus.OK.value())
                    .text("Comments retrieved successfully")
                    .data(jsonData)
                    .build();
        } catch (JsonProcessingException e) {
            return ResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .text("Failed to retrieve comments: " + e.getMessage())
                    .build();
        }
    }

    public ResponseDto getCommentsByPublicationId(UUID publicationId) {
        try {
            // Rechercher uniquement les commentaires de premier niveau (pas les réponses)
            List<Comment> comments = commentRepository.findByPublicationIdAndParentIdIsNull(publicationId);
            
            // Pour chaque commentaire, charger ses réponses
            comments.forEach(comment -> {
                List<Comment> replies = commentRepository.findByParentId(comment.getId());
                comment.setReplies(replies);
            });
            
            String jsonData = objectMapper.writeValueAsString(comments);
            return ResponseDto.builder()
                    .status(HttpStatus.OK.value())
                    .text("Comments for target retrieved successfully")
                    .data(jsonData)
                    .build();
        } catch (JsonProcessingException e) {
            return ResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .text("Failed to retrieve comments: " + e.getMessage())
                    .build();
        }
    }

    public ResponseDto createComment(CommentRequest request) {
        try {
            // Vérification que la publication existe
            if (request.getPublicationId() != null && 
                publicationRepository.findById(request.getPublicationId()).isEmpty()) {
                return ResponseDto.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .text("Target publication not found")
                        .build();
            }
            
            // Vérification que l'utilisateur existe
            if (userRepository.findById(request.getUserId()).isEmpty()) {
                return ResponseDto.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .text("User not found")
                        .build();
            }
            
            // Vérification que le commentaire parent existe (si applicable)
            if (request.getParentId() != null && 
                commentRepository.findById(request.getParentId()).isEmpty()) {
                return ResponseDto.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .text("Parent comment not found")
                        .build();
            }
            
            Comment comment = Comment.builder()
                    .content(request.getContent())
                    .publicationId(request.getPublicationId())
                    .userId(request.getUserId())
                    .parentId(request.getParentId())
                    .build();
            
            Comment savedComment = commentRepository.save(comment);
            String jsonData = objectMapper.writeValueAsString(savedComment);
            
            return ResponseDto.builder()
                    .status(HttpStatus.CREATED.value())
                    .text("Comment created successfully")
                    .data(jsonData)
                    .build();
        } catch (JsonProcessingException e) {
            return ResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .text("Failed to create comment: " + e.getMessage())
                    .build();
        }
    }

    public ResponseDto getCommentById(UUID id) {
        try {
            Optional<Comment> comment = commentRepository.findById(id);
            if (comment.isEmpty()) {
                return ResponseDto.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .text("Comment not found")
                        .build();
            }
            
            // Si c'est un commentaire parent, charger ses réponses
            if (comment.get().getParentId() == null) {
                List<Comment> replies = commentRepository.findByParentId(comment.get().getId());
                comment.get().setReplies(replies);
            }
            
            String jsonData = objectMapper.writeValueAsString(comment.get());
            
            return ResponseDto.builder()
                    .status(HttpStatus.OK.value())
                    .text("Comment retrieved successfully")
                    .data(jsonData)
                    .build();
        } catch (JsonProcessingException e) {
            return ResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .text("Failed to retrieve comment: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ResponseDto updateComment(UUID id, CommentRequest request) {
        try {
            Optional<Comment> existingComment = commentRepository.findById(id);
            if (existingComment.isEmpty()) {
                return ResponseDto.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .text("Comment not found")
                        .build();
            }
            
            Comment comment = existingComment.get();
            
            // Vérifier que l'utilisateur qui met à jour est le créateur du commentaire
            if (!comment.getUserId().equals(request.getUserId())) {
                return ResponseDto.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .text("You are not authorized to update this comment")
                        .build();
            }
            
            comment.setContent(request.getContent());
            
            Comment updatedComment = commentRepository.save(comment);
            String jsonData = objectMapper.writeValueAsString(updatedComment);
            
            return ResponseDto.builder()
                    .status(HttpStatus.OK.value())
                    .text("Comment updated successfully")
                    .data(jsonData)
                    .build();
        } catch (JsonProcessingException e) {
            return ResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .text("Failed to update comment: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ResponseDto deleteComment(UUID id, UUID userId) {
        try {
            Optional<Comment> comment = commentRepository.findById(id);
            if (comment.isEmpty()) {
                return ResponseDto.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .text("Comment not found")
                        .build();
            }
            
            // Vérifier que l'utilisateur qui supprime est le créateur du commentaire
            if (!comment.get().getUserId().equals(userId)) {
                return ResponseDto.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .text("You are not authorized to delete this comment")
                        .build();
            }
            
            // Supprimer toutes les réponses à ce commentaire
            List<Comment> replies = commentRepository.findByParentId(id);
            commentRepository.deleteAll(replies);
            
            // Supprimer le commentaire
            commentRepository.delete(comment.get());
            
            return ResponseDto.builder()
                    .status(HttpStatus.OK.value())
                    .text("Comment deleted successfully")
                    .build();
        } catch (Exception e) {
            return ResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .text("Failed to delete comment: " + e.getMessage())
                    .build();
        }
    }
}
