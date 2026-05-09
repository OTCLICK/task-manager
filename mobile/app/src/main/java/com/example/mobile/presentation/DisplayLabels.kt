package com.example.mobile.presentation

fun participationRoleRu(role: String): String = when (role) {
    "ORGANIZER" -> "Организатор"
    "COORDINATOR" -> "Координатор"
    "PERFORMER" -> "Исполнитель"
    else -> role
}

fun eventStatusRu(status: String): String = when (status) {
    "PLANNED" -> "Запланировано"
    "ACTIVE" -> "Идёт"
    "COMPLETED" -> "Завершено"
    "CANCELLED" -> "Отменено"
    else -> status
}

fun taskStatusRu(status: String): String = when (status) {
    "CREATED" -> "Создана"
    "IN_PROGRESS" -> "В работе"
    "HELP_REQUESTED" -> "Нужна помощь"
    "COMPLETED" -> "Выполнена"
    "CANCELLED" -> "Отменена"
    else -> status
}

fun taskPriorityRu(priority: String): String = when (priority) {
    "LOW" -> "Низкий"
    "MEDIUM" -> "Средний"
    "HIGH" -> "Высокий"
    else -> priority
}

fun invitationStatusRu(status: String): String = when (status) {
    "PENDING" -> "Ожидает ответа"
    "ACCEPTED" -> "Принято"
    "DECLINED" -> "Отклонено"
    else -> status
}

/** Значения, которые уходят в API (порядок — для меню). */
val EVENT_STATUS_OPTIONS: List<String> = listOf("PLANNED", "ACTIVE", "COMPLETED", "CANCELLED")

val TASK_STATUS_OPTIONS: List<String> = listOf(
    "CREATED",
    "IN_PROGRESS",
    "HELP_REQUESTED",
    "COMPLETED",
    "CANCELLED"
)

val TASK_PRIORITY_OPTIONS: List<String> = listOf("LOW", "MEDIUM", "HIGH")
