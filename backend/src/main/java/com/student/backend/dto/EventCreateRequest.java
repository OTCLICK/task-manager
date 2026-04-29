package com.student.backend.dto;

import com.student.backend.model.EventStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record EventCreateRequest(

        @NotBlank(message = "Название мероприятия не должно быть пустым")
        @Size(max = 40, message = "Название мероприятия не должно превышать 40 символов")
        String name,

        @NotBlank(message = "Адрес мероприятия не должен быть пустым")
        String address,

        @PositiveOrZero(message = "Количество участников не может быть отрицательным")
        int participatesCount,

        EventStatus status,

        @FutureOrPresent
        LocalDateTime startTime,

        @Future
        LocalDateTime endTime
) {
}


