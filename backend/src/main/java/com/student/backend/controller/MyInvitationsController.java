package com.student.backend.controller;

import com.student.backend.dto.InvitationResponse;
import com.student.backend.dto.SentInvitationResponse;
import com.student.backend.security.SecurityUtils;
import com.student.backend.service.ParticipationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
public class MyInvitationsController {

    private final ParticipationService participationService;
    private final SecurityUtils securityUtils;

    public MyInvitationsController(ParticipationService participationService, SecurityUtils securityUtils) {
        this.participationService = participationService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<InvitationResponse>> getIncomingPending() {
        String userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(participationService.getIncomingPendingInvitationsForUser(userId));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<SentInvitationResponse>> getSent() {
        String userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(participationService.getSentInvitationsForUser(userId));
    }
}
