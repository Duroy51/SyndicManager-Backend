package com.enspy.syndicmanager.controllers;

import com.enspy.syndicmanager.dto.request.ReactionRequest;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.models.Reaction;
import com.enspy.syndicmanager.services.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5176", allowedHeaders = "*")
public class ReactionController {

    private final ReactionService reactionService;

    @GetMapping
    public ResponseEntity<ResponseDto> getAllReactions() {
        ResponseDto response = reactionService.getAllReactions();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/publication/{publicationId}")
    public ResponseEntity<ResponseDto> getReactionsByPublicationId(@PathVariable UUID publicationId) {
        ResponseDto response = reactionService.getReactionsByPublicationId(publicationId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createReaction(@RequestBody ReactionRequest request) {
        ResponseDto response = reactionService.createReaction(request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ResponseDto> removeReaction(
            @RequestParam UUID userId,
            @RequestParam UUID targetId,
            @RequestParam Reaction.ReactionType reactionType) {
        ResponseDto response = reactionService.removeReaction(userId, targetId, reactionType);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
