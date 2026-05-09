package com.student.backend.dto;

import com.student.backend.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusPatchRequest(
        @NotNull(message = "Статус обязателен")
        TaskStatus taskStatus
) {
}
