package com.student.backend.service;

import com.student.backend.dto.EventCreateRequest;
import com.student.backend.dto.EventResponse;
import com.student.backend.dto.FullNameDto;
import com.student.backend.dto.UserResponse;
import com.student.backend.model.Event;
import com.student.backend.model.User;
import com.student.backend.model.UserRole;
import com.student.backend.repository.EventRepository;
import com.student.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Мероприятие не найдено: " + id));
        return toEventResponse(event);
    }

    public EventResponse createEvent(EventCreateRequest request, String organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Организатор не найден: " + organizerId));

        if (organizer.getRole() != UserRole.ORGANIZER) {
            throw new IllegalArgumentException("Только организатор может создавать мероприятия");
        }

        Event event = new Event(
                request.name(),
                request.address(),
                request.status(),
                request.startTime(),
                request.endTime(),
                organizer
        );

        Event savedEvent = eventRepository.save(event);
        return toEventResponse(savedEvent);
    }

    public void deleteEvent(String id) {
        if (!eventRepository.existsById(id)) {
            throw new IllegalArgumentException("Мероприятие не найдено: " + id);
        }
        eventRepository.deleteById(id);
    }

    private EventResponse toEventResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getAddress(),
                event.getParticipatesCount(),
                event.getStatus(),
                event.getStartTime(),
                event.getEndTime(),
                toUserResponse(event.getOrganizer())
        );
    }

    private UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                new FullNameDto(
                        user.getFullName().getName(),
                        user.getFullName().getSurname(),
                        user.getFullName().getPatronymic()
                ),
                user.getRole()
        );
    }
}
