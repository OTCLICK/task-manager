package com.student.backend.integration;

import com.student.backend.dto.AuthRequest;
import com.student.backend.support.AbstractIntegrationTest;
import com.student.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты аутентификации (регистрация, вход, ошибки).
 */
class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registerAndLogin_success() throws Exception {
        String email = "auth-user-" + System.nanoTime() + "@test.local";
        IntegrationTestSupport.registerAndGetToken(mockMvc, objectMapper, email);

        AuthRequest login = new AuthRequest(email, IntegrationTestSupport.VALID_PASSWORD);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void register_duplicateEmail_returnsBadRequest() throws Exception {
        String email = "dup-" + System.nanoTime() + "@test.local";
        IntegrationTestSupport.registerAndGetToken(mockMvc, objectMapper, email);

        var req = IntegrationTestSupport.userCreateRequest(email, "Пётр", "Петров");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_wrongPassword_returnsBadRequest() throws Exception {
        String email = "wrong-pass-" + System.nanoTime() + "@test.local";
        IntegrationTestSupport.registerAndGetToken(mockMvc, objectMapper, email);

        AuthRequest login = new AuthRequest(email, "WrongPass9!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest());
    }
}
