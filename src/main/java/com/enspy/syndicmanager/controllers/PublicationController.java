package com.enspy.syndicmanager.controllers;

import com.enspy.syndicmanager.dto.request.PublicationRequest;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.services.PublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService publicationService;

    @GetMapping
    public ResponseEntity<ResponseDto> getAllPublications() {
        ResponseDto response = publicationService.getAllPublications();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getPublicationById(@PathVariable UUID id) {
        ResponseDto response = publicationService.getPublicationById(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createPublication(@RequestBody PublicationRequest request) {
        ResponseDto response = publicationService.createPublication(request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> updatePublication(
            @PathVariable UUID id,
            @RequestBody PublicationRequest request
    ) {
        ResponseDto response = publicationService.updatePublication(id, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deletePublication(@PathVariable UUID id) {
        ResponseDto response = publicationService.deletePublication(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
