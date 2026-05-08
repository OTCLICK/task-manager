package com.student.backend.service;

import com.student.backend.dto.*;
import com.student.backend.exception.AccessDeniedException;
import com.student.backend.exception.NotFoundException;
import com.student.backend.exception.ValidationException;
import com.student.backend.model.*;
import com.student.backend.repository.EventRepository;
import com.student.backend.repository.ParticipationRepository;
import com.student.backend.repository.UserRepository;
import com.student.backend.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;

    public ZoneService(ZoneRepository zoneRepository,
                       EventRepository eventRepository,
                       UserRepository userRepository,
                       ParticipationRepository participationRepository) {
        this.zoneRepository = zoneRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.participationRepository = participationRepository;
    }

    @Transactional(readOnly = true)
    public List<ZoneResponse> getAllZones() {
        return zoneRepository.findAll().stream()
                .map(this::toZoneResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ZoneResponse getZoneById(String id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Зона не найдена"));
        return toZoneResponse(zone);
    }

    public ZoneResponse createZone(ZoneCreateRequest request, String coordinatorId) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new NotFoundException("Мероприятие не найдено"));

        User coordinator = userRepository.findById(coordinatorId)
                .orElseThrow(() -> new NotFoundException("Координатор не найден"));

//        if (coordinator.getRole() != UserRole.COORDINATOR) {
//            throw new ValidationException("Пользователь должен быть координатором");
//        }

        Participation participation = participationRepository.findByUserIdAndEventId(coordinatorId, request.eventId())
                .orElseThrow(() -> new AccessDeniedException("Вы не участвуете в мероприятии"));

        if (participation.getRole() != UserRole.ORGANIZER && participation.getRole() != UserRole.COORDINATOR) {
            throw new AccessDeniedException("Только организатор и координатор могут создавать зоны");
        }

        int participatesCount = (request.participatesCount() != null) ? request.participatesCount() : 0;

        Zone zone = new Zone(
                request.name(),
                request.description(),
                participatesCount,
                event,
                coordinator
        );

        Zone savedZone = zoneRepository.save(zone);
        return toZoneResponse(savedZone);
    }

    public void deleteZone(String id, String coordinatorId) {
        Zone zone = zoneRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Зона не найдена"));

        if (!zone.getCoordinator().getId().equals(coordinatorId)) {
            throw new AccessDeniedException("Недостаточно прав для удаления зоны");
        }
        zoneRepository.deleteById(id);
    }

    private ZoneResponse toZoneResponse(Zone zone) {
        return new ZoneResponse(
                zone.getId(),
                zone.getName(),
                zone.getDescription(),
                zone.getParticipatesCount(),
                toEventResponse(zone.getEvent()),
                toUserResponse(zone.getCoordinator())
        );
    }

    private EventResponse toEventResponse(Event event) {
        if (event == null) return null;
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
                )
//                user.getRole()
        );
    }
}

