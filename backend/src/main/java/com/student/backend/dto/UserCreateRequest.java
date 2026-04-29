package com.student.backend.dto;

import com.student.backend.model.UserRole;
import jakarta.validation.constraints.*;

public record UserCreateRequest(
        @NotBlank(message = "Email не должен быть пустым")
        @Email
        String email,

        @NotBlank(message = "Пароль не должен быть пустым")
        @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
        @Pattern(regexp = ".*[A-ZА-ЯЁ].*", message = "Пароль должен содержать хотя бы одну заглавную букву")
        @Pattern(regexp = ".*[^a-zA-Zа-яА-Я0-9ёЁ].*", message = "Пароль должен содержать хотя бы один специальный символ")
        String password,

        @NotNull
        FullNameDto fullName,

        @NotNull
        UserRole role
) {
}