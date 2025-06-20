package com.enspy.syndicmanager.controllers;

import com.enspy.syndicmanager.dto.request.CommentRequest;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<ResponseDto> getAllComments() {
        ResponseDto response = commentService.getAllComments();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/publication/{publicationId}")
    public ResponseEntity<ResponseDto> getCommentsByPublicationId(@PathVariable UUID publicationId) {
        ResponseDto response = commentService.getCommentsByPublicationId(publicationId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getCommentById(@PathVariable UUID id) {
        ResponseDto response = commentService.getCommentById(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/add")
    public ResponseEntity<ResponseDto> createComment(@RequestBody CommentRequest request) {
        ResponseDto response = commentService.createComment(request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> updateComment(
            @PathVariable UUID id,
            @RequestBody CommentRequest request) {
        ResponseDto response = commentService.updateComment(id, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteComment(
            @PathVariable UUID id,
            @RequestParam UUID userId) {
        ResponseDto response = commentService.deleteComment(id, userId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
