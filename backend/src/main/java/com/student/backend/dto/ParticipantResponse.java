package com.student.backend.dto;

import com.student.backend.model.UserRole;

public record ParticipantResponse(
        String userId,
        String email,
        UserRole role
) {}
