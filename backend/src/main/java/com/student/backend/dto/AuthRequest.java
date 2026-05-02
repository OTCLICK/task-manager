package com.student.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(

        @NotBlank(message = "Email не должен быть пустым")
        @Email
        String email,

        @NotBlank
        String password

) {
}
