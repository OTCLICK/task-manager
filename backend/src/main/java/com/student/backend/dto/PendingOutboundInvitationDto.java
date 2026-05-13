package com.student.backend.dto;

public record PendingOutboundInvitationDto(
        String invitationId,
        String invitedUserEmail
) {
}
