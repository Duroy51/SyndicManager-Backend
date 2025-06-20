package com.enspy.syndicmanager.controllers;

import com.enspy.syndicmanager.dto.request.EventRequest;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<ResponseDto> getAllEvents() {
        ResponseDto response = eventService.getAllEvents();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ResponseDto> getUpcomingEvents() {
        ResponseDto response = eventService.getUpcomingEvents();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/past")
    public ResponseEntity<ResponseDto> getPastEvents() {
        ResponseDto response = eventService.getPastEvents();
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getEventById(@PathVariable UUID id) {
        ResponseDto response = eventService.getEventById(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createEvent(@RequestBody EventRequest request) {
        ResponseDto response = eventService.createEvent(request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> updateEvent(
            @PathVariable UUID id,
            @RequestBody EventRequest request
    ) {
        ResponseDto response = eventService.updateEvent(id, request);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteEvent(@PathVariable UUID id) {
        ResponseDto response = eventService.deleteEvent(id);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @PostMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<ResponseDto> addParticipant(
            @PathVariable UUID eventId,
            @PathVariable UUID userId
    ) {
        ResponseDto response = eventService.addParticipant(eventId, userId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }

    @DeleteMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<ResponseDto> removeParticipant(
            @PathVariable UUID eventId,
            @PathVariable UUID userId
    ) {
        ResponseDto response = eventService.removeParticipant(eventId, userId);
        return ResponseEntity.status(response.statusCode()).body(response);
    }
}
