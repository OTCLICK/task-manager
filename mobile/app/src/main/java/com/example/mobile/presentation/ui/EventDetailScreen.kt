package com.example.mobile.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobile.data.model.Zone
import com.example.mobile.presentation.viewmodel.EventDetailViewModel

private fun normalizeLocalDateTimeInput(raw: String): String {
    val t = raw.trim()
    if (t.isEmpty()) return t
    return t.replace(Regex("T(\\d{2})-(\\d{2})$"), "T$1:$2")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showZoneDialog by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<String?>(null) }

    if (showZoneDialog) {
        CreateZoneDialog(
            onDismiss = {
                showZoneDialog = false
                viewModel.clearError()
            },
            onConfirm = { name, desc, count ->
                viewModel.createZone(name, desc, count) { ok ->
                    if (ok) showZoneDialog = false
                }
            },
            errorText = state.formError
        )
    }

    if (showTaskDialog) {
        CreateTaskDialog(
            zones = state.zones,
            onDismiss = {
                showTaskDialog = false
                viewModel.clearError()
            },
            onConfirm = { title, desc, zoneId, priority, deadline ->
                viewModel.createTask(title, desc, zoneId, priority, deadline) { ok ->
                    if (ok) showTaskDialog = false
                }
            },
            errorText = state.formError
        )
    }

    taskToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Удалить задачу?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTask(id)
                        taskToDelete = null
                    }
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.event?.name ?: "Мероприятие") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.refresh() }) {
                        Text("Обновить")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isLoading && state.event == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                state.event?.let { ev ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(ev.address, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Статус: ${ev.status}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                if (state.errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.clearError() }
                    ) {
                        Text(
                            text = state.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showZoneDialog = true },
                        modifier = Modifier.weight(1f)
                    ) { Text("Новая зона") }
                    Button(
                        onClick = { showTaskDialog = true },
                        modifier = Modifier.weight(1f)
                    ) { Text("Новая задача") }
                }
            }

            item {
                Text("Зоны", style = MaterialTheme.typography.titleMedium)
            }
            if (state.zones.isEmpty()) {
                item {
                    Text("Зон пока нет", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(state.zones, key = { it.id }) { zone ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(zone.name, style = MaterialTheme.typography.titleSmall)
                            zone.description?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Задачи",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (state.tasks.isEmpty()) {
                item {
                    Text(
                        "Задач по этому мероприятию пока нет",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(state.tasks, key = { it.id }) { task ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(task.title, style = MaterialTheme.typography.titleSmall)
                            task.description?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                "Статус: ${task.status} · Приоритет: ${task.priority}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = task.zone?.let { z -> "Зона: ${z.name}" } ?: "Зона: не указана",
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = { taskToDelete = task.id }) {
                                Text("Удалить", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateZoneDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?, participatesCount: Int?) -> Unit,
    errorText: String?
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var countStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая зона") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = countStr,
                    onValueChange = { countStr = it },
                    label = { Text("Участников (число)") },
                    modifier = Modifier.fillMaxWidth()
                )
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            description.trim().takeIf { it.isNotEmpty() },
                            countStr.toIntOrNull()
                        )
                    }
                }
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun CreateTaskDialog(
    zones: List<Zone>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, zoneId: String?, priority: String?, deadline: String?) -> Unit,
    errorText: String?
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var expanded by remember { mutableStateOf(false) }
    var selectedZone by remember { mutableStateOf<Zone?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedZone?.name ?: "Без зоны",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Зона (необязательно)") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать зону")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Без зоны") },
                            onClick = {
                                selectedZone = null
                                expanded = false
                            }
                        )
                        zones.forEach { zone ->
                            DropdownMenuItem(
                                text = { Text(zone.name) },
                                onClick = {
                                    selectedZone = zone
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it.uppercase() },
                    label = { Text("Приоритет (LOW/MEDIUM/HIGH)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Дедлайн (ГГГГ-ММ-ДДTЧЧ:ММ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title.trim(),
                            description.trim().takeIf { it.isNotEmpty() },
                            selectedZone?.id,
                            priority.takeIf { it.isNotBlank() },
                            normalizeLocalDateTimeInput(deadline).takeIf { it.isNotBlank() }
                        )
                    }
                }
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
