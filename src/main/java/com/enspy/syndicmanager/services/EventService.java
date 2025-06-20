package com.enspy.syndicmanager.services;

import com.enspy.syndicmanager.dto.request.EventRequest;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.models.Event;
import com.enspy.syndicmanager.models.User;
import com.enspy.syndicmanager.repositories.EventRepository;
import com.enspy.syndicmanager.repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ResponseDto getAllEvents() {
        List<Event> events = eventRepository.findAllByOrderByStartDateDesc();
        return createSuccessResponse("Events retrieved successfully", events);
    }

    public ResponseDto getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByStartDateAfterOrderByStartDateAsc(now);
        return createSuccessResponse("Upcoming events retrieved successfully", events);
    }

    public ResponseDto getPastEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findByStartDateBeforeOrderByStartDateDesc(now);
        return createSuccessResponse("Past events retrieved successfully", events);
    }

    public ResponseDto getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return createSuccessResponse("Event retrieved successfully", event);
    }

    public ResponseDto createEvent(EventRequest request) {
        User author = null;
        if (request.getAuthorId() != null) {
            author = userRepository.findById(request.getAuthorId())
                    .orElse(null);
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .authorName(request.getAuthorName())
                .authorAvatar(request.getAuthorAvatar())
                .author(author)
                .images(request.getImages() != null ? request.getImages() : new ArrayList<>())
                .category(request.getCategory())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .notifyMembers(request.getNotifyMembers() != null ? request.getNotifyMembers() : true)
                .isUpcoming(request.getStartDate().isAfter(LocalDateTime.now()))
                .participants(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        Event savedEvent = eventRepository.save(event);
        return createSuccessResponse("Event created successfully", savedEvent);
    }

    public ResponseDto updateEvent(UUID id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setImages(request.getImages() != null ? request.getImages() : event.getImages());
        event.setCategory(request.getCategory());
        event.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : event.getIsPublic());
        event.setNotifyMembers(request.getNotifyMembers() != null ? request.getNotifyMembers() : event.getNotifyMembers());
        event.setIsUpcoming(request.getStartDate().isAfter(LocalDateTime.now()));

        Event updatedEvent = eventRepository.save(event);
        return createSuccessResponse("Event updated successfully", updatedEvent);
    }

    public ResponseDto deleteEvent(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
        return createSuccessResponse("Event deleted successfully", null);
    }

    public ResponseDto addParticipant(UUID eventId, UUID userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (!event.getParticipants().contains(user)) {
            event.getParticipants().add(user);
            eventRepository.save(event);
        }
        
        return createSuccessResponse("Participant added successfully", event);
    }

    public ResponseDto removeParticipant(UUID eventId, UUID userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        event.getParticipants().remove(user);
        eventRepository.save(event);
        
        return createSuccessResponse("Participant removed successfully", event);
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
