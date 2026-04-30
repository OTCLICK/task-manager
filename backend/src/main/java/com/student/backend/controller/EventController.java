package com.student.backend.controller;

import com.student.backend.dto.EventCreateRequest;
import com.student.backend.dto.EventResponse;
import com.student.backend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
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

//    TODO:
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @RequestParam String organizerId // временно, пока нет авторизации
    ) {
        EventResponse event = eventService.createEvent(request, organizerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
