package com.student.backend.dto;

import com.student.backend.model.InvitationStatus;
import com.student.backend.model.UserRole;

import java.time.LocalDateTime;

public record SentInvitationResponse(
        String invitationId,
        String eventId,
        String eventName,
        String invitedUserId,
        String invitedUserEmail,
        UserRole role,
        InvitationStatus status,
        LocalDateTime createdAt
) {
}
