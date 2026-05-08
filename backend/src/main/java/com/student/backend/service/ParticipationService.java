package com.student.backend.service;

import com.student.backend.dto.ParticipantResponse;
import com.student.backend.exception.AccessDeniedException;
import com.student.backend.exception.NotFoundException;
import com.student.backend.exception.ValidationException;
import com.student.backend.model.Event;
import com.student.backend.model.Participation;
import com.student.backend.model.User;
import com.student.backend.model.UserRole;
import com.student.backend.repository.EventRepository;
import com.student.backend.repository.ParticipationRepository;
import com.student.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public ParticipationService(
            ParticipationRepository participationRepository,
            UserRepository userRepository,
            EventRepository eventRepository
    ) {
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public void createOrganizerParticipation(String userId, String eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие не найдено"));

        participationRepository.save(new Participation(user, event, UserRole.ORGANIZER));
    }

    public void joinEventAsPerformer(String userId, String eventId) {
        if (participationRepository.findByUserIdAndEventId(userId, eventId).isPresent()) {
            throw new ValidationException("Пользователь уже участвует в мероприятии");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие не найдено"));

        participationRepository.save(new Participation(user, event, UserRole.PERFORMER));
    }

    public void changeParticipantRole(String organizerId, String eventId, String targetUserId, UserRole newRole) {
        Participation organizerParticipation = participationRepository
                .findByUserIdAndEventId(organizerId, eventId)
                .orElseThrow(() -> new AccessDeniedException("Вы не участвуете в этом мероприятии"));

        if (organizerParticipation.getRole() != UserRole.ORGANIZER) {
            throw new AccessDeniedException("Только организатор может менять роли");
        }

        Participation targetParticipation = participationRepository
                .findByUserIdAndEventId(targetUserId, eventId)
                .orElseThrow(() -> new NotFoundException("Участник не найден в мероприятии"));

        if (targetUserId.equals(organizerId)) {
            throw new ValidationException("Нельзя изменить свою роль");
        }
        if (targetParticipation.getRole() == UserRole.ORGANIZER) {
            throw new ValidationException("Нельзя изменить роль другого организатора");
        }

        if (newRole != UserRole.COORDINATOR && newRole != UserRole.PERFORMER) {
            throw new ValidationException("Недопустимая роль");
        }

        targetParticipation.setRole(newRole);
        participationRepository.save(targetParticipation);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponse> getParticipants(String eventId) {
        return participationRepository.findByEventId(eventId).stream()
                .map(p -> new ParticipantResponse(
                        p.getUser().getId(),
                        p.getUser().getEmail(),
                        p.getRole()
                ))
                .collect(Collectors.toList());
    }
}
