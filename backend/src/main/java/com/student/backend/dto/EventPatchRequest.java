package com.student.backend.dto;

import com.student.backend.model.EventStatus;
import jakarta.validation.constraints.NotNull;

public record EventPatchRequest(
        @NotNull(message = "Статус обязателен")
        EventStatus status
) {}
