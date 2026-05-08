package com.student.backend.dto;

import com.student.backend.model.UserRole;

public record ParticipationResponse(String eventId, UserRole role) {
}
