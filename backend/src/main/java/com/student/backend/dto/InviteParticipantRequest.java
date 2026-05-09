package com.student.backend.dto;

import com.student.backend.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteParticipantRequest(
        @NotBlank(message = "Email не должен быть пустым")
        @Email(message = "Некорректный email")
        String email,
        UserRole role
) {
}
