package com.example.mobile.presentation.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobile.data.model.ParticipantApiModel
import com.example.mobile.data.model.Task
import com.example.mobile.data.model.Zone
import com.example.mobile.presentation.TASK_PRIORITY_OPTIONS
import com.example.mobile.presentation.TASK_STATUS_OPTIONS
import com.example.mobile.presentation.eventStatusRu
import com.example.mobile.presentation.participationRoleRu
import com.example.mobile.presentation.taskPriorityRu
import com.example.mobile.presentation.taskStatusRu
import com.example.mobile.presentation.viewmodel.EventDetailViewModel

private const val ROLE_PERFORMER = "PERFORMER"

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
            participants = state.participants,
            onDismiss = {
                showTaskDialog = false
                viewModel.clearError()
            },
            onConfirm = { title, desc, zoneId, priority, deadline, performerIds ->
                viewModel.createTask(title, desc, zoneId, priority, deadline, performerIds) { ok ->
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
                                "Статус: ${eventStatusRu(ev.status)}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                state.cacheHint?.let { hint ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.dismissCacheHint() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
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
                    TaskCardWithStatus(
                        task = task,
                        onStatusChange = { newStatus ->
                            viewModel.updateTaskStatus(task.id, newStatus)
                        },
                        onDelete = { taskToDelete = task.id }
                    )
                }
            }
        }
    }
}

@Composable
private fun taskPriorityCardColors(priority: String): CardColors {
    val scheme = MaterialTheme.colorScheme
    return when (priority.uppercase()) {
        "HIGH" -> CardDefaults.cardColors(
            containerColor = scheme.errorContainer,
            contentColor = scheme.onErrorContainer
        )
        "MEDIUM" -> CardDefaults.cardColors(
            containerColor = scheme.tertiaryContainer,
            contentColor = scheme.onTertiaryContainer
        )
        "LOW" -> CardDefaults.cardColors(
            containerColor = scheme.secondaryContainer,
            contentColor = scheme.onSecondaryContainer
        )
        else -> CardDefaults.cardColors()
    }
}

@Composable
private fun taskPriorityAccentColor(priority: String) = when (priority.uppercase()) {
    "HIGH" -> MaterialTheme.colorScheme.error
    "MEDIUM" -> MaterialTheme.colorScheme.tertiary
    "LOW" -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.outline
}

@Composable
private fun TaskCardWithStatus(
    task: Task,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var statusMenuOpen by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = taskPriorityCardColors(task.priority),
        border = BorderStroke(
            width = 1.dp,
            color = taskPriorityAccentColor(task.priority).copy(alpha = 0.55f)
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleSmall)
            task.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "Приоритет: ${taskPriorityRu(task.priority)}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = taskPriorityAccentColor(task.priority),
                modifier = Modifier.padding(top = 4.dp)
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = taskStatusRu(task.status),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Статус") },
                    trailingIcon = {
                        IconButton(onClick = { statusMenuOpen = !statusMenuOpen }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать статус")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { statusMenuOpen = true }
                )
                DropdownMenu(
                    expanded = statusMenuOpen,
                    onDismissRequest = { statusMenuOpen = false }
                ) {
                    TASK_STATUS_OPTIONS.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(taskStatusRu(s)) },
                            onClick = {
                                statusMenuOpen = false
                                if (s != task.status) onStatusChange(s)
                            }
                        )
                    }
                }
            }
            Text(
                text = task.zone?.let { z -> "Зона: ${z.name}" } ?: "Зона: не указана",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (task.performers.isNotEmpty()) {
                Text(
                    text = "Исполнители: " + task.performers.joinToString { it.email },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            TextButton(onClick = onDelete) {
                Text("Удалить", color = MaterialTheme.colorScheme.error)
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
    participants: List<ParticipantApiModel>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, zoneId: String?, priority: String?, deadline: String?, performerIds: List<String>?) -> Unit,
    errorText: String?
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }
    var priorityExpanded by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedZone by remember { mutableStateOf<Zone?>(null) }
    var selectedPerformerIds by remember { mutableStateOf(setOf<String>()) }

    val performerCandidates = remember(participants) {
        participants.filter { it.role == ROLE_PERFORMER }
    }

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
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = taskPriorityRu(priority),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Приоритет") },
                        trailingIcon = {
                            IconButton(onClick = { priorityExpanded = !priorityExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать приоритет")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { priorityExpanded = true }
                    )
                    DropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        TASK_PRIORITY_OPTIONS.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(taskPriorityRu(code)) },
                                onClick = {
                                    priority = code
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Дедлайн (ГГГГ-ММ-ДДTЧЧ:ММ)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Исполнители (${participationRoleRu("PERFORMER")})",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (performerCandidates.isEmpty()) {
                    Text(
                        "В мероприятии нет участников с ролью исполнителя — назначить никого нельзя (по правилам сервера).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    performerCandidates.forEach { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedPerformerIds.contains(p.userId),
                                onCheckedChange = { checked ->
                                    selectedPerformerIds = if (checked) {
                                        selectedPerformerIds + p.userId
                                    } else {
                                        selectedPerformerIds - p.userId
                                    }
                                }
                            )
                            Text(
                                text = p.email,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val ids = selectedPerformerIds.toList().takeIf { it.isNotEmpty() }
                        onConfirm(
                            title.trim(),
                            description.trim().takeIf { it.isNotEmpty() },
                            selectedZone?.id,
                            priority.takeIf { it.isNotBlank() },
                            normalizeLocalDateTimeInput(deadline).takeIf { it.isNotBlank() },
                            ids
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
