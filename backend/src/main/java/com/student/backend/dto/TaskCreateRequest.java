package com.student.backend.dto;

import com.student.backend.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record TaskCreateRequest(

        @NotBlank(message = "Название не должно быть пустым")
        @Size(max = 64, message = "Название не должно превышать 64 символа")
        String title,

        @Size(max = 1600, message = "Описание не должно превышать 1600 символов")
        String description,

        TaskPriority taskPriority,

        String zoneId,

        List<String> performers,

        LocalDateTime deadline,

        @NotBlank
        String eventId

) {}
