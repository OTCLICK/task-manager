package com.student.backend.integration;

import com.student.backend.support.AbstractIntegrationTest;
import com.student.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Дымовое нагрузочное тестирование: параллельные запросы к API списка мероприятий.
 * Для диплома фиксируются время и доля успешных ответов (без внешнего JMeter).
 */
class ApiLoadSmokeTest extends AbstractIntegrationTest {

  private static final int CONCURRENT_REQUESTS = 25;

  @Test
  void parallelGetEvents_allSucceedWithinTimeLimit() throws Exception {
    String email = "load-" + System.nanoTime() + "@test.local";
    String token = IntegrationTestSupport.registerAndGetToken(mockMvc, objectMapper, email);
    IntegrationTestSupport.createEvent(mockMvc, objectMapper, token, "Load test event");

    ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
    try {
      List<Callable<Long>> tasks = new ArrayList<>();
      for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
        tasks.add(() -> {
          long start = System.nanoTime();
          mockMvc.perform(get("/api/events").header("Authorization", "Bearer " + token))
              .andExpect(status().isOk());
          return System.nanoTime() - start;
        });
      }

      long wallStart = System.nanoTime();
      List<Future<Long>> results = pool.invokeAll(tasks, 30, TimeUnit.SECONDS);
      long wallMs = (System.nanoTime() - wallStart) / 1_000_000;

      assertThat(results).hasSize(CONCURRENT_REQUESTS);
      for (Future<Long> f : results) {
        assertThat(f.get()).isPositive();
      }

      long maxMs = results.stream().mapToLong(f -> {
        try {
          return f.get() / 1_000_000;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }).max().orElse(0);

      // Порог для in-memory H2 + MockMvc; на проде измеряют отдельно (JMeter/k6).
      assertThat(wallMs).isLessThan(15_000);
      assertThat(maxMs).isLessThan(5_000);

      System.out.printf(
          "Load smoke: %d parallel GET /api/events, wall=%d ms, max single=%d ms%n",
          CONCURRENT_REQUESTS, wallMs, maxMs
      );
    } finally {
      pool.shutdownNow();
    }
  }
}
