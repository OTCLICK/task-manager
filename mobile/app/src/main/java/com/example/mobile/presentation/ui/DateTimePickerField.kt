package com.example.mobile.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val zone: ZoneId get() = ZoneId.systemDefault()

private val ruDateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale("ru", "RU"))

fun LocalDateTime.toApiDateTimeString(): String =
    withSecond(0).withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

private fun LocalDateTime?.formatForDisplay(placeholder: String): String =
    this?.format(ruDateTimeFormatter) ?: placeholder

private fun LocalDateTime?.toStartOfDayMillis(): Long? =
    this?.toLocalDate()?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    value: LocalDateTime?,
    onValueChange: (LocalDateTime?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Не выбрано — нажмите, чтобы выбрать",
    allowClear: Boolean = true
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDateMillis by remember { mutableStateOf<Long?>(null) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value.formatForDisplay(placeholder),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = "Выбрать дату и время")
                }
            },
            modifier = Modifier
                .weight(1f)
                .clickable { showDatePicker = true }
        )
        if (allowClear && value != null) {
            IconButton(
                onClick = { onValueChange(null) }
            ) {
                Icon(Icons.Filled.Clear, contentDescription = "Сбросить")
            }
        }
    }

    if (showDatePicker) {
        key(value, showDatePicker) {
            val initialMillis = value?.toStartOfDayMillis()
                ?: LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val picked = datePickerState.selectedDateMillis
                            if (picked != null) {
                                pendingDateMillis = picked
                                showDatePicker = false
                                showTimePicker = true
                            }
                        }
                    ) { Text("Далее") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    if (showTimePicker) {
        key(pendingDateMillis, value, showTimePicker) {
            val pickedDate = pendingDateMillis?.let { ms ->
                Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
            } ?: value?.toLocalDate() ?: LocalDate.now(zone)
            val timeSeed =
                if (value != null && value.toLocalDate() == pickedDate) value.toLocalTime()
                else LocalTime.of(9, 0)
            val timePickerState = rememberTimePickerState(
                initialHour = timeSeed.hour,
                initialMinute = timeSeed.minute,
                is24Hour = true
            )
            AlertDialog(
                onDismissRequest = {
                    showTimePicker = false
                    pendingDateMillis = null
                },
                title = { Text("Время") },
                text = {
                    TimePicker(state = timePickerState)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val pm = pendingDateMillis ?: return@TextButton
                            val date = Instant.ofEpochMilli(pm).atZone(zone).toLocalDate()
                            val t = LocalTime.of(timePickerState.hour, timePickerState.minute, 0)
                            onValueChange(LocalDateTime.of(date, t))
                            pendingDateMillis = null
                            showTimePicker = false
                        }
                    ) { Text("Готово") }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showTimePicker = false
                            pendingDateMillis = null
                        }
                    ) { Text("Отмена") }
                }
            )
        }
    }
}
