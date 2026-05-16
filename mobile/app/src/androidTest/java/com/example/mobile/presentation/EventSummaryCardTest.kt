package com.example.mobile.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mobile.data.model.EventApiModel
import com.example.mobile.presentation.ui.EventSummaryCard
import com.example.mobile.presentation.ui.theme.MobileTheme
import org.junit.Rule
import org.junit.Test

class EventSummaryCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsAddressAndStatus() {
        val event = EventApiModel(
            eventId = "e1",
            name = "Конференция",
            address = "Москва, зал 1",
            participatesCount = 50,
            status = "PLANNED",
            startTime = null,
            endTime = null
        )
        composeRule.setContent {
            MobileTheme {
                EventSummaryCard(
                    event = event,
                    canPatchEventStatus = false,
                    onSelectEventStatus = {}
                )
            }
        }
        composeRule.onNodeWithText("Москва, зал 1").assertIsDisplayed()
        composeRule.onNodeWithText("Статус: Запланировано").assertIsDisplayed()
    }
}
