package com.enspy.syndicmanager.services;

import com.enspy.syndicmanager.dto.request.PublicationRequest;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.models.Publication;
import com.enspy.syndicmanager.models.User;
import com.enspy.syndicmanager.repositories.PublicationRepository;
import com.enspy.syndicmanager.repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ResponseDto getAllPublications() {
        List<Publication> publications = publicationRepository.findAllByOrderByCreatedAtDesc();
        return createSuccessResponse("Publications retrieved successfully", publications);
    }

    public ResponseDto getPublicationById(UUID id) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication not found with id: " + id));
        return createSuccessResponse("Publication retrieved successfully", publication);
    }

    public ResponseDto createPublication(PublicationRequest request) {
        User author = null;
        if (request.getAuthorId() != null) {
            author = userRepository.findById(request.getAuthorId())
                    .orElse(null);
        }

        Publication publication = Publication.builder()
                .content(request.getContent())
                .image(request.getImage())
                .authorName(request.getAuthorName())
                .authorAvatar(request.getAuthorAvatar())
                .author(author)
                .createdAt(LocalDateTime.now())
                .build();

        Publication savedPublication = publicationRepository.save(publication);
        return createSuccessResponse("Publication created successfully", savedPublication);
    }

    public ResponseDto updatePublication(UUID id, PublicationRequest request) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication not found with id: " + id));

        publication.setContent(request.getContent());
        publication.setImage(request.getImage());
        
        Publication updatedPublication = publicationRepository.save(publication);
        return createSuccessResponse("Publication updated successfully", updatedPublication);
    }

    public ResponseDto deletePublication(UUID id) {
        if (!publicationRepository.existsById(id)) {
            throw new RuntimeException("Publication not found with id: " + id);
        }
        publicationRepository.deleteById(id);
        return createSuccessResponse("Publication deleted successfully", null);
    }

    private ResponseDto createSuccessResponse(String message, Object data) {
        String dataJson = null;
        try {
            if (data != null) {
                dataJson = objectMapper.writeValueAsString(data);
            }
        } catch (JsonProcessingException e) {
            return ResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .text("Error processing response data")
                    .data(null)
                    .build();
        }
        
        return ResponseDto.builder()
                .status(HttpStatus.OK.value())
                .text(message)
                .data(dataJson)
                .build();
    }
}
