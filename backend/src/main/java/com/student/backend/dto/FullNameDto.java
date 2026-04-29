package com.student.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record FullNameDto(
        @NotBlank(message = "Имя не может должно пустым")
        @Pattern(regexp = "^[А-Яа-яЁёA-Za-z]+$", message = "Имя должно содержать только буквы")
        String name,

        @NotBlank(message = "Фамилия не должна быть пустой")
        @Pattern(regexp = "^[А-Яа-яЁёA-Za-z]+$", message = "Фамилия должна содержать только буквы")
        String surname,

        @Pattern(regexp = "^[А-Яа-яЁёA-Za-z]+$", message = "Отчество должно содержать только буквы")
        String patronymic
) {
}
