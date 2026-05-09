package com.student.backend.dto;

import com.student.backend.model.InvitationStatus;
import com.student.backend.model.UserRole;

import java.time.LocalDateTime;

public record InvitationResponse(
        String invitationId,
        String eventId,
        String eventName,
        String invitedByUserId,
        String invitedByEmail,
        UserRole role,
        InvitationStatus status,
        LocalDateTime createdAt
) {
}
