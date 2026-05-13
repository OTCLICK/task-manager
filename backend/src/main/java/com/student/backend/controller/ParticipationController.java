package com.student.backend.controller;

import com.student.backend.dto.ParticipantResponse;
import com.student.backend.dto.InviteParticipantRequest;
import com.student.backend.dto.InvitationResponse;
import com.student.backend.dto.PendingOutboundInvitationDto;
import com.student.backend.model.UserRole;
import com.student.backend.security.CustomUserDetails;
import com.student.backend.service.ParticipationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/participants")
public class ParticipationController {

    private final ParticipationService participationService;

    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    // Вступить в мероприятие
    @PostMapping("/join")
    public ResponseEntity<Void> joinEvent(@PathVariable String eventId, Authentication auth) {
        String userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        participationService.joinEventAsPerformer(userId, eventId);
        return ResponseEntity.ok().build();
    }

    // Пригласить участника в мероприятие (только организатор)
    @PostMapping("/invite")
    public ResponseEntity<Void> inviteParticipant(
            @PathVariable String eventId,
            @Valid @RequestBody InviteParticipantRequest request,
            Authentication auth
    ) {
        String organizerId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        participationService.inviteParticipant(organizerId, eventId, request.email(), request.role());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/invitations/my")
    public ResponseEntity<List<InvitationResponse>> getMyPendingInvitations(
            @PathVariable String eventId,
            Authentication auth
    ) {
        String userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        return ResponseEntity.ok(participationService.getPendingInvitations(eventId, userId));
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable String eventId,
            @PathVariable String invitationId,
            Authentication auth
    ) {
        String userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        participationService.acceptInvitation(eventId, invitationId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invitations/{invitationId}/decline")
    public ResponseEntity<Void> declineInvitation(
            @PathVariable String eventId,
            @PathVariable String invitationId,
            Authentication auth
    ) {
        String userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        participationService.declineInvitation(eventId, invitationId, userId);
        return ResponseEntity.ok().build();
    }

    /** Исходящие приглашения в ожидании (только для организатора мероприятия). */
    @GetMapping("/invitations/pending-outbound")
    public ResponseEntity<List<PendingOutboundInvitationDto>> getPendingOutboundInvitations(
            @PathVariable String eventId,
            Authentication auth
    ) {
        String userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        return ResponseEntity.ok(participationService.getPendingOutboundInvitations(userId, eventId));
    }

    // Изменить роль участника (только организатор)
    @PatchMapping("/{participantId}/role")
    public ResponseEntity<Void> changeRole(
            @PathVariable String eventId,
            @PathVariable String participantId,
            @RequestParam UserRole newRole,
            Authentication auth
    ) {
        String userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        participationService.changeParticipantRole(userId, eventId, participantId, newRole);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{participantId}/remove")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable String eventId,
            @PathVariable String participantId,
            Authentication auth
    ) {
        String userId = ((CustomUserDetails) auth.getPrincipal()).getUserId();
        participationService.removeParticipant(userId, eventId, participantId);
        return ResponseEntity.ok().build();
    }

    // Список участников
    @GetMapping
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable String eventId) {
        return ResponseEntity.ok(participationService.getParticipants(eventId));
    }
}