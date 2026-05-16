# Тестирование Task Manager

## Стратегия

| Уровень | Backend | Mobile |
|--------|---------|--------|
| Модульный | JUnit 5 + Mockito: сервисы, JWT | JUnit 4: `DisplayLabels`, форматирование |
| Интеграционный | Spring Boot + MockMvc + H2 | — |
| UI | — | Jetpack Compose Test (`EventSummaryCard`) |
| Нагрузочный (дымовой) | `ApiLoadSmokeTest`: 25 параллельных GET | — |

## Инструменты и обоснование

- **JUnit 5** — стандарт для Spring Boot 3+/Java 21, хорошая интеграция с Maven.
- **Mockito** — изоляция сервисов без БД (быстрые unit-тесты).
- **MockMvc** — HTTP-слой без поднятия реального сервера; проверка security + JSON.
- **H2 (test)** — in-memory БД, воспроизводимые интеграционные тесты без PostgreSQL.
- **JaCoCo** — отчёт покрытия кода для раздела диплома.
- **Jetpack Compose UI Test** — декларативный UI, проверка отображения без Espresso-селекторов по id.

## Запуск

### Backend

```bash
cd backend
./mvnw test
./mvnw test jacoco:report
# отчёт: backend/target/site/jacoco/index.html
```

Профиль `test`: `src/test/resources/application-test.properties`, демо-данные (`InitDataLoader`) отключены.

### Mobile

```bash
cd mobile
./gradlew test                    # unit (JVM)
./gradlew connectedDebugAndroidTest   # UI на эмуляторе/устройстве
```

## Покрываемые сценарии

1. Регистрация и вход (`AuthIntegrationTest`).
2. Создание мероприятия, приглашение участников, задача, join/decline (`TaskWorkflowIntegrationTest`).
3. Запрет создания зоны исполнителем (`ZoneServiceTest`, `TaskWorkflowIntegrationTest`).
4. PATCH статуса мероприятия и отчёт по задачам.
5. Mute зон уведомлений (`ZoneNotificationPreferenceServiceTest`).
6. JWT: генерация и отклонение подделанного токена (`JwtUtilsTest`).
7. Отображение ролей/статусов на клиенте (`DisplayLabelsTest`).
8. Карточка мероприятия в Compose (`EventSummaryCardTest`).

## Нагрузочное тестирование

`ApiLoadSmokeTest` выполняет 25 параллельных авторизованных запросов `GET /api/events` и выводит в консоль суммарное и максимальное время. Для промышленной нагрузки рекомендуется **JMeter** или **k6** против развёрнутого backend на PostgreSQL.

## Метрики для диплома

После `./mvnw test jacoco:report` (отчёт: `backend/target/site/jacoco/index.html`):

| Метрика | Значение (пример на текущем наборе тестов) |
|--------|---------------------------------------------|
| Покрытие инструкций (JaCoCo) | ~48 % |
| Покрытие ветвлений | ~22 % |
| Тестов backend | 14 (JUnit) |
| Тестов mobile (unit) | 7+ (`DisplayLabelsTest`) |

Нагрузочный дымовой тест (`ApiLoadSmokeTest`): 25 параллельных `GET /api/events`, 100 % HTTP 200, wall ≈ 30 ms, max single ≈ 25 ms (H2 + MockMvc, не production).

- Количество тестов: `Tests run: N` в выводе Maven.
- Примеры исправленных дефектов (из истории разработки): доступ исполнителя к зонам, join только при HELP_REQUESTED — зафиксированы регрессионными тестами.

## Пример формулировки для п. 5.5

*«Разработана многоуровневая стратегия тестирования: модульные тесты бизнес-логики (JUnit, Mockito), интеграционные тесты REST API (Spring MockMvc, H2), UI-тесты мобильного клиента (Compose Test). Покрытие backend по JaCoCo составило X %. Дымовое нагрузочное тестирование 25 параллельных запросов показало 100 % успешных ответов при времени отклика до Y мс в тестовом окружении.»*
