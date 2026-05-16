package com.student.backend.integration;

import com.student.backend.support.AbstractIntegrationTest;
import com.student.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Сквозной сценарий: мероприятие → задача → присоединение исполнителя → снятие с задачи.
 */
class TaskWorkflowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void performerJoinsAndDeclinesTask() throws Exception {
        long n = System.nanoTime();
        String orgEmail = "org-" + n + "@test.local";
        String coordEmail = "coord-" + n + "@test.local";
        String perfEmail = "perf-" + n + "@test.local";
        String perf2Email = "perf2-" + n + "@test.local";

        String orgToken = IntegrationTestSupport.registerAndGetToken(mockMvc, objectMapper, orgEmail);
        IntegrationTestSupport.registerAndGetToken(mockMvc, objectMapper, coordEmail);
        IntegrationTestSupport.registerAndGetToken(mockMvc, objectMapper, perfEmail);
        String perf2Token = IntegrationTestSupport.registerAndGetToken(mockMvc, objectMapper, perf2Email);

        String eventId = IntegrationTestSupport.createEvent(mockMvc, objectMapper, orgToken, "Мероприятие " + n);
        IntegrationTestSupport.inviteAndAccept(
                mockMvc, objectMapper, orgToken,
                IntegrationTestSupport.loginAndGetToken(mockMvc, objectMapper, coordEmail),
                eventId, coordEmail, "COORDINATOR"
        );
        IntegrationTestSupport.inviteAndAccept(
                mockMvc, objectMapper, orgToken,
                IntegrationTestSupport.loginAndGetToken(mockMvc, objectMapper, perfEmail),
                eventId, perfEmail, "PERFORMER"
        );
        IntegrationTestSupport.inviteAndAccept(
                mockMvc, objectMapper, orgToken, perf2Token, eventId, perf2Email, "PERFORMER"
        );

        String coordToken = IntegrationTestSupport.loginAndGetToken(mockMvc, objectMapper, coordEmail);
        String perfId = IntegrationTestSupport.getUserId(mockMvc, objectMapper,
                IntegrationTestSupport.loginAndGetToken(mockMvc, objectMapper, perfEmail));

        String taskId = IntegrationTestSupport.createTask(
                mockMvc, objectMapper, coordToken, eventId, "Задача A", List.of(perfId)
        );

        mockMvc.perform(post("/api/tasks/" + taskId + "/join-as-performer")
                        .header("Authorization", "Bearer " + perf2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.performers.length()").value(2));

        mockMvc.perform(post("/api/tasks/" + taskId + "/decline-self")
                        .header("Authorization", "Bearer " + perf2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.performers.length()").value(1));
    }

    @Test
    void performerCannotCreateZone() throws Exception {
        long n = System.nanoTime();
        String orgEmail = "org-z-" + n + "@test.local";
        String perfEmail = "perf-z-" + n + "@test.local";

        String orgToken = IntegrationTestSupport.registerAndGetToken(mockMvc, objectMapper, orgEmail);
        String perfToken = IntegrationTestSupport.registerAndGetToken(mockMvc, objectMapper, perfEmail);

        String eventId = IntegrationTestSupport.createEvent(mockMvc, objectMapper, orgToken, "Зоны " + n);
        IntegrationTestSupport.inviteAndAccept(
                mockMvc, objectMapper, orgToken, perfToken, eventId, perfEmail, "PERFORMER"
        );

        String zoneBody = """
                {"name":"Зона 1","description":"тест","participatesCount":5,"eventId":"%s"}
                """.formatted(eventId);
        mockMvc.perform(post("/api/zones")
                        .header("Authorization", "Bearer " + perfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zoneBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizerCanPatchEventStatusAndGetReport() throws Exception {
        long n = System.nanoTime();
        String orgEmail = "org-r-" + n + "@test.local";
        String orgToken = IntegrationTestSupport.registerAndGetToken(mockMvc, objectMapper, orgEmail);
        String eventId = IntegrationTestSupport.createEvent(mockMvc, objectMapper, orgToken, "Отчёт " + n);

        String patchBody = "{\"status\":\"ACTIVE\"}";
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/events/" + eventId)
                        .header("Authorization", "Bearer " + orgToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/events/" + eventId + "/task-report")
                        .header("Authorization", "Bearer " + orgToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.tasksByStatus").exists());
    }
}
