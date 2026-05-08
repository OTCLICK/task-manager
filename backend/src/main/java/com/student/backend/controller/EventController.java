package com.student.backend.controller;

import com.student.backend.dto.EventCreateRequest;
import com.student.backend.dto.EventResponse;
import com.student.backend.security.SecurityUtils;
import com.student.backend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final SecurityUtils securityUtils;

    public EventController(EventService eventService, SecurityUtils securityUtils) {
        this.eventService = eventService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        String organizerId = securityUtils.getCurrentUserId();
        EventResponse event = eventService.createEvent(request, organizerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        String organizerId = securityUtils.getCurrentUserId();
        eventService.deleteEvent(id,  organizerId);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/{eventId}/my-role")
//    public ResponseEntity<String> getMyRoleInEvent(
//            @PathVariable String eventId,
//            Authentication auth
//    ) {
//        String userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
//        return participationRepository.findByUserIdAndEventId(userId, eventId)
//                .map(p -> ResponseEntity.ok(p.getRoleName()))
//                .orElse(ResponseEntity.notFound().build());
//    }
}
