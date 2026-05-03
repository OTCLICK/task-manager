package com.student.backend.dto;

import com.student.backend.model.Event;
import com.student.backend.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ZoneCreateRequest(
        @NotBlank(message = "Название зоны не должно быть пустым")
        String name,

        @Size(max = 1200, message = "Описание зоны не должно превышать 1200 символов")
        String description,

        @PositiveOrZero(message = "Количество участников не может быть отрицательным")
        Integer participatesCount,

        @NotBlank
        String eventId
) {
}