package com.student.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FcmTokenRequest(
        @NotBlank(message = "Токен FCM не может быть пустым")
        @Size(max = 512)
        String token
) {}
