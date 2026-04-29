package com.student.backend.dto;

import com.student.backend.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record TaskCreateRequest(

        @NotBlank(message = "Название не должно быть пустым")
        @Size(max = 16, message = "Название не должно превышать 16 символов")
        String title,

        @Size(max = 400, message = "Описание не должно превышать 400 символов")
        String description,

        TaskPriority taskPriority,

        String zoneId,

        List<String> performers,

        LocalDateTime deadline

) {}
