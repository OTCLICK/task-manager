package com.student.backend.service;

import com.student.backend.dto.FullNameDto;
import com.student.backend.dto.ParticipantResponse;
import com.student.backend.dto.InvitationResponse;
import com.student.backend.dto.PendingOutboundInvitationDto;
import com.student.backend.dto.SentInvitationResponse;
import com.student.backend.exception.AccessDeniedException;
import com.student.backend.exception.NotFoundException;
import com.student.backend.exception.ValidationException;
import com.student.backend.model.Event;
import com.student.backend.model.EventInvitation;
import com.student.backend.model.InvitationStatus;
import com.student.backend.model.Participation;
import com.student.backend.model.User;
import com.student.backend.model.UserRole;
import com.student.backend.repository.EventRepository;
import com.student.backend.repository.EventInvitationRepository;
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
    private final EventInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public ParticipationService(
            ParticipationRepository participationRepository,
            EventInvitationRepository invitationRepository,
            UserRepository userRepository,
            EventRepository eventRepository
    ) {
        this.participationRepository = participationRepository;
        this.invitationRepository = invitationRepository;
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

    public void inviteParticipant(String organizerId, String eventId, String email, UserRole role) {
        Participation organizerParticipation = participationRepository
                .findByUserIdAndEventId(organizerId, eventId)
                .orElseThrow(() -> new AccessDeniedException("Вы не участвуете в этом мероприятии"));

        if (organizerParticipation.getRole() != UserRole.ORGANIZER) {
            throw new AccessDeniedException("Только организатор может приглашать участников");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким email не найден"));

        if (participationRepository.findByUserIdAndEventId(user.getId(), eventId).isPresent()) {
            throw new ValidationException("Пользователь уже участвует в мероприятии");
        }

        UserRole targetRole = role == null ? UserRole.PERFORMER : role;
        if (targetRole != UserRole.PERFORMER && targetRole != UserRole.COORDINATOR) {
            throw new ValidationException("При приглашении допустимы только роли PERFORMER или COORDINATOR");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие не найдено"));

        if (invitationRepository.findFirstByEventIdAndInvitedUserIdAndStatus(eventId, user.getId(), InvitationStatus.PENDING).isPresent()) {
            throw new ValidationException("Для пользователя уже есть активное приглашение в это мероприятие");
        }

        User inviter = userRepository.findById(organizerId)
                .orElseThrow(() -> new NotFoundException("Организатор не найден"));

        EventInvitation invitation = new EventInvitation(
                event,
                user,
                inviter,
                targetRole,
                InvitationStatus.PENDING,
                java.time.LocalDateTime.now()
        );
        invitationRepository.save(invitation);
    }

    public void acceptInvitation(String eventId, String invitationId, String invitedUserId) {
        EventInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Приглашение не найдено"));
        if (!invitation.getEvent().getId().equals(eventId)) {
            throw new ValidationException("Приглашение не относится к этому мероприятию");
        }

        if (!invitation.getInvitedUser().getId().equals(invitedUserId)) {
            throw new AccessDeniedException("Недостаточно прав для принятия приглашения");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ValidationException("Приглашение уже обработано");
        }
        if (participationRepository.findByUserIdAndEventId(invitedUserId, invitation.getEvent().getId()).isPresent()) {
            throw new ValidationException("Вы уже участвуете в этом мероприятии");
        }

        participationRepository.save(new Participation(
                invitation.getInvitedUser(),
                invitation.getEvent(),
                invitation.getRole()
        ));

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(java.time.LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    public void declineInvitation(String eventId, String invitationId, String invitedUserId) {
        EventInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Приглашение не найдено"));
        if (!invitation.getEvent().getId().equals(eventId)) {
            throw new ValidationException("Приглашение не относится к этому мероприятию");
        }

        if (!invitation.getInvitedUser().getId().equals(invitedUserId)) {
            throw new AccessDeniedException("Недостаточно прав для отклонения приглашения");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ValidationException("Приглашение уже обработано");
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        invitation.setRespondedAt(java.time.LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getPendingInvitations(String eventId, String invitedUserId) {
        return invitationRepository.findByEventIdAndInvitedUserIdAndStatus(eventId, invitedUserId, InvitationStatus.PENDING)
                .stream()
                .map(i -> new InvitationResponse(
                        i.getId(),
                        i.getEvent().getId(),
                        i.getEvent().getName(),
                        i.getInvitedBy().getId(),
                        i.getInvitedBy().getEmail(),
                        i.getRole(),
                        i.getStatus(),
                        i.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getIncomingPendingInvitationsForUser(String invitedUserId) {
        return invitationRepository.findByInvitedUserIdAndStatus(invitedUserId, InvitationStatus.PENDING)
                .stream()
                .map(i -> new InvitationResponse(
                        i.getId(),
                        i.getEvent().getId(),
                        i.getEvent().getName(),
                        i.getInvitedBy().getId(),
                        i.getInvitedBy().getEmail(),
                        i.getRole(),
                        i.getStatus(),
                        i.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SentInvitationResponse> getSentInvitationsForUser(String invitedByUserId) {
        return invitationRepository.findByInvitedByIdOrderByCreatedAtDesc(invitedByUserId)
                .stream()
                .map(i -> new SentInvitationResponse(
                        i.getId(),
                        i.getEvent().getId(),
                        i.getEvent().getName(),
                        i.getInvitedUser().getId(),
                        i.getInvitedUser().getEmail(),
                        i.getRole(),
                        i.getStatus(),
                        i.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PendingOutboundInvitationDto> getPendingOutboundInvitations(String userId, String eventId) {
        Participation self = participationRepository
                .findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new AccessDeniedException("Вы не участвуете в этом мероприятии"));
        if (self.getRole() != UserRole.ORGANIZER) {
            throw new AccessDeniedException("Только организатор может просматривать отправленные приглашения");
        }
        return invitationRepository
                .findPendingOutboundForEventAndInviter(eventId, userId, InvitationStatus.PENDING)
                .stream()
                .map(i -> new PendingOutboundInvitationDto(i.getId(), i.getInvitedUser().getEmail()))
                .collect(Collectors.toList());
    }

    public void withdrawSentInvitation(String currentUserId, String eventId, String invitationId) {
        EventInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Приглашение не найдено"));
        if (!inv.getEvent().getId().equals(eventId)) {
            throw new NotFoundException("Приглашение не относится к этому мероприятию");
        }
        if (!inv.getInvitedBy().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Можно отменить только собственное приглашение");
        }
        if (inv.getStatus() != InvitationStatus.PENDING) {
            throw new ValidationException("Приглашение уже обработано или отменено");
        }
        inv.setStatus(InvitationStatus.CANCELLED);
        inv.setRespondedAt(java.time.LocalDateTime.now());
        invitationRepository.save(inv);
    }

    /** Отзыв по id приглашения (eventId берётся из сущности — тот же путь, что у хаба /api/invitations). */
    public void withdrawSentInvitationById(String currentUserId, String invitationId) {
        EventInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Приглашение не найдено"));
        withdrawSentInvitation(currentUserId, inv.getEvent().getId(), invitationId);
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

    public void removeParticipant(String organizerId, String eventId, String targetUserId) {
        Participation organizerParticipation = participationRepository
                .findByUserIdAndEventId(organizerId, eventId)
                .orElseThrow(() -> new AccessDeniedException("Вы не участвуете в этом мероприятии"));

        if (organizerParticipation.getRole() != UserRole.ORGANIZER) {
            throw new AccessDeniedException("Только организатор может исключать участников");
        }

        Participation targetParticipation = participationRepository
                .findByUserIdAndEventId(targetUserId, eventId)
                .orElseThrow(() -> new NotFoundException("Участник не найден в мероприятии"));

        if (targetUserId.equals(organizerId)) {
            throw new ValidationException("Нельзя исключить себя из мероприятия");
        }
        if (targetParticipation.getRole() == UserRole.ORGANIZER) {
            throw new ValidationException("Нельзя исключить организатора");
        }

        participationRepository.delete(targetParticipation);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponse> getParticipants(String eventId) {
        return participationRepository.findByEventId(eventId).stream()
                .map(p -> {
                    var fn = p.getUser().getFullName();
                    FullNameDto fullNameDto = new FullNameDto(
                            fn.getName(),
                            fn.getSurname(),
                            fn.getPatronymic()
                    );
                    return new ParticipantResponse(
                            p.getUser().getId(),
                            p.getUser().getEmail(),
                            p.getRole(),
                            fullNameDto
                    );
                })
                .collect(Collectors.toList());
    }
}
