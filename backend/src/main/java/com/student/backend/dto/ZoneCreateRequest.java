package com.student.backend.dto;

import com.student.backend.model.Event;
import com.student.backend.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ZoneCreateRequest(
        @NotBlank(message = "Название зоны не должно быть пустым")
        String name,

        @Size(max = 300, message = "Описание зоны не должно превышать 300 символов")
        String description,

        @PositiveOrZero(message = "Количество участников не может быть отрицательным")
        int participatesCount
) {
}