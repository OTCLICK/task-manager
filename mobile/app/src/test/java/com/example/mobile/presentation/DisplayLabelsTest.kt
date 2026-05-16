package com.example.mobile.presentation

import com.example.mobile.data.model.FullName
import com.example.mobile.data.model.ParticipantApiModel
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Модульные тесты отображения статусов и ролей (слой presentation).
 */
class DisplayLabelsTest {

    @Test
    fun formatFullNameRu_withPatronymic() {
        val name = formatFullNameRu(FullName("Иван", "Иванов", "Иванович"))
        assertEquals("Иванов Иван Иванович", name)
    }

    @Test
    fun formatFullNameRu_withoutPatronymic() {
        val name = formatFullNameRu(FullName("Мария", "Петрова", null))
        assertEquals("Петрова Мария", name)
    }

    @Test
    fun participantDisplayTitle_prefersFullName() {
        val p = ParticipantApiModel(
            userId = "1",
            email = "a@test.local",
            role = "PERFORMER",
            fullName = FullName("Анна", "Смирнова", null)
        )
        assertEquals("Смирнова Анна", participantDisplayTitle(p))
    }

    @Test
    fun participantDisplayTitle_fallsBackToEmail() {
        val p = ParticipantApiModel(
            userId = "1",
            email = "only@test.local",
            role = "COORDINATOR",
            fullName = null
        )
        assertEquals("only@test.local", participantDisplayTitle(p))
    }

    @Test
    fun taskStatusRu_knownStatuses() {
        assertEquals("В работе", taskStatusRu("IN_PROGRESS"))
        assertEquals("Нужна помощь", taskStatusRu("HELP_REQUESTED"))
        assertEquals("Выполнена", taskStatusRu("COMPLETED"))
    }

    @Test
    fun eventStatusRu_knownStatuses() {
        assertEquals("Запланировано", eventStatusRu("PLANNED"))
        assertEquals("Идёт", eventStatusRu("ACTIVE"))
    }

    @Test
    fun participationRoleRu_knownRoles() {
        assertEquals("Организатор", participationRoleRu("ORGANIZER"))
        assertEquals("Исполнитель", participationRoleRu("PERFORMER"))
    }
}
