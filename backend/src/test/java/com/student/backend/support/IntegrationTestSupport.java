package com.student.backend.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.student.backend.dto.AuthRequest;
import com.student.backend.dto.AuthResponse;
import com.student.backend.dto.EventCreateRequest;
import com.student.backend.dto.FullNameDto;
import com.student.backend.dto.TaskCreateRequest;
import com.student.backend.dto.UserCreateRequest;
import com.student.backend.model.EventStatus;
import com.student.backend.model.TaskPriority;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Вспомогательные методы для интеграционных тестов API.
 */
public final class IntegrationTestSupport {

    private IntegrationTestSupport() {}

    public static final String VALID_PASSWORD = "TestPass1!";

    public static UserCreateRequest userCreateRequest(String email, String name, String surname) {
        return new UserCreateRequest(
                email,
                VALID_PASSWORD,
                new FullNameDto(name, surname, null)
        );
    }

    public static String registerAndGetToken(MockMvc mockMvc, ObjectMapper mapper, String email) throws Exception {
        UserCreateRequest req = userCreateRequest(email, "Иван", "Иванов");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse auth = mapper.readValue(result.getResponse().getContentAsByteArray(), AuthResponse.class);
        return auth.token();
    }

    public static String loginAndGetToken(MockMvc mockMvc, ObjectMapper mapper, String email) throws Exception {
        AuthRequest req = new AuthRequest(email, VALID_PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse auth = mapper.readValue(result.getResponse().getContentAsByteArray(), AuthResponse.class);
        return auth.token();
    }

    public static String createEvent(MockMvc mockMvc, ObjectMapper mapper, String organizerToken, String name) throws Exception {
        EventCreateRequest req = new EventCreateRequest(
                name,
                "ул. Тестовая, 1",
                10,
                EventStatus.PLANNED,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(7)
        );
        MvcResult result = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andReturn();
        if (result.getResponse().getStatus() != 201) {
            throw new AssertionError(
                    "POST /api/events expected 201 but was " + result.getResponse().getStatus()
                            + ": " + result.getResponse().getContentAsString()
            );
        }
        JsonNode node = mapper.readTree(result.getResponse().getContentAsByteArray());
        return node.get("eventId").asText();
    }

    public static void addParticipant(
            MockMvc mockMvc,
            ObjectMapper mapper,
            String organizerToken,
            String eventId,
            String email,
            String role
    ) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"role\":\"" + role + "\"}";
        mockMvc.perform(post("/api/events/" + eventId + "/participants/invite")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    public static String createTask(
            MockMvc mockMvc,
            ObjectMapper mapper,
            String coordinatorToken,
            String eventId,
            String title,
            List<String> performerIds
    ) throws Exception {
        TaskCreateRequest req = new TaskCreateRequest(
                title,
                "Описание",
                TaskPriority.MEDIUM,
                null,
                performerIds,
                null,
                eventId
        );
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + coordinatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsByteArray());
        return node.get("taskId").asText();
    }

    public static String getUserId(MockMvc mockMvc, ObjectMapper mapper, String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsByteArray());
        return node.get("userId").asText();
    }

    public static void inviteAndAccept(
            MockMvc mockMvc,
            ObjectMapper mapper,
            String organizerToken,
            String inviteeToken,
            String eventId,
            String inviteeEmail,
            String role
    ) throws Exception {
        String inviteBody = "{\"email\":\"" + inviteeEmail + "\",\"role\":\"" + role + "\"}";
        mockMvc.perform(post("/api/events/" + eventId + "/participants/invite")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inviteBody))
                .andExpect(status().isOk());

        MvcResult incoming = mockMvc.perform(get("/api/invitations/incoming")
                        .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode invitations = mapper.readTree(incoming.getResponse().getContentAsByteArray());
        String invitationId = null;
        for (JsonNode inv : invitations) {
            if (eventId.equals(inv.get("eventId").asText())) {
                invitationId = inv.get("invitationId").asText();
                break;
            }
        }
        if (invitationId == null) {
            throw new IllegalStateException("Приглашение не найдено для eventId=" + eventId);
        }
        mockMvc.perform(post("/api/events/" + eventId + "/participants/invitations/" + invitationId + "/accept")
                        .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isOk());
    }
}
