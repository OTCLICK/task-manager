package com.example.mobile.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobile.data.model.EventCreateRequest
import com.example.mobile.presentation.EVENT_STATUS_OPTIONS
import com.example.mobile.presentation.eventStatusRu
import com.example.mobile.presentation.viewmodel.CreateEventViewModel

/** Частая опечатка: T10-00 вместо T10:00 для ISO-8601 LocalDateTime на сервере. */
private fun normalizeLocalDateTimeInput(raw: String): String {
    val t = raw.trim()
    if (t.isEmpty()) return t
    return t.replace(Regex("T(\\d{2})-(\\d{2})$"), "T$1:$2")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onEventCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateEventViewModel
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var participantsCount by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("PLANNED") }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isCreated) {
        if (state.isCreated) {
            viewModel.resetCreated()
            onEventCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать мероприятие") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Адрес *") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it },
                label = { Text("Начало (ГГГГ-ММ-ДДTЧЧ:ММ)") },
                placeholder = { Text("2026-06-01T10:00") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            OutlinedTextField(
                value = endTime,
                onValueChange = { endTime = it },
                label = { Text("Окончание (ГГГГ-ММ-ДДTЧЧ:ММ)") },
                placeholder = { Text("2026-06-01T18:00") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            OutlinedTextField(
                value = participantsCount,
                onValueChange = { participantsCount = it },
                label = { Text("Количество участников") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                OutlinedTextField(
                    value = eventStatusRu(status),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Статус мероприятия") },
                    trailingIcon = {
                        IconButton(onClick = { statusMenuExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать статус")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { statusMenuExpanded = true }
                )
                DropdownMenu(
                    expanded = statusMenuExpanded,
                    onDismissRequest = { statusMenuExpanded = false }
                ) {
                    EVENT_STATUS_OPTIONS.forEach { code ->
                        DropdownMenuItem(
                            text = { Text(eventStatusRu(code)) },
                            onClick = {
                                status = code
                                statusMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (name.isBlank() || address.isBlank()) {
                        // Покажите ошибку через snackbar или toast
                        return@Button
                    }

                    val count = participantsCount.toIntOrNull()
                    val startNormalized = normalizeLocalDateTimeInput(startTime)
                    val endNormalized = normalizeLocalDateTimeInput(endTime)
                    val request = EventCreateRequest(
                        name = name,
                        address = address,
                        participatesCount = count,
                        status = if (status.isNotBlank()) status else "PLANNED",
                        startTime = startNormalized.takeIf { it.isNotBlank() },
                        endTime = endNormalized.takeIf { it.isNotBlank() }
                    )
                    viewModel.createEvent(request)
                },
                enabled = !state.isLoading && name.isNotBlank() && address.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Создать мероприятие")
                }
            }

            if (state.errorMessage != null) {
                Text(
                    text = "Ошибка: ${state.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}