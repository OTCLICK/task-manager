package com.student.backend.controller;

import com.student.backend.dto.ParticipantResponse;
import com.student.backend.model.UserRole;
import com.student.backend.security.CustomUserDetails;
import com.student.backend.service.ParticipationService;
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

    // Список участников
    @GetMapping
    public ResponseEntity<List<ParticipantResponse>> getParticipants(@PathVariable String eventId) {
        return ResponseEntity.ok(participationService.getParticipants(eventId));
    }
}