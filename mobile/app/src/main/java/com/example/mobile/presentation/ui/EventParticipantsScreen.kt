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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mobile.data.model.ParticipantApiModel
import com.example.mobile.presentation.participantDisplayTitle
import com.example.mobile.presentation.participationRoleRu
import com.example.mobile.presentation.viewmodel.EventParticipantsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventParticipantsScreen(
    viewModel: EventParticipantsViewModel,
    onBack: () -> Unit,
    onOpenUserProfile: (userId: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var roleMenuUserId by remember { mutableStateOf<String?>(null) }
    var pendingRemove by remember { mutableStateOf<ParticipantApiModel?>(null) }

    val visibleParticipants = remember(state.participants, state.debouncedSearchQuery) {
        val q = state.debouncedSearchQuery
        if (q.isBlank()) {
            state.participants
        } else {
            val needle = q.lowercase()
            state.participants.filter { p ->
                val hay = buildString {
                    append(participantDisplayTitle(p).lowercase())
                    append(' ')
                    append(p.email.lowercase())
                }
                hay.contains(needle)
            }
        }
    }

    pendingRemove?.let { p ->
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            title = { Text("Исключить участника?") },
            text = {
                Text(
                    "${participantDisplayTitle(p)} будет удалён из списка участников мероприятия."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeParticipant(p.userId)
                        pendingRemove = null
                    },
                    enabled = !state.actionInProgress
                ) { Text("Исключить") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemove = null }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Участники")
                        state.eventTitle?.let { t ->
                            Text(
                                t,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.refresh() },
                        enabled = !state.isLoading && !state.actionInProgress
                    ) {
                        Text("Обновить")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading && state.participants.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

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
                        hint,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            state.errorMessage?.let { err ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.clearMessages() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        err,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            state.successMessage?.let { ok ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.clearMessages() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        ok,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                "Поиск по ФИО или email. Список фильтруется после паузы во вводе.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Поиск участников") },
                leadingIcon = {
                    Icon(Icons.Filled.Person, contentDescription = null)
                }
            )

            if (state.actionInProgress) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            val total = state.participants.size
            val shown = visibleParticipants.size
            Text(
                if (state.debouncedSearchQuery.isNotBlank() && shown != total) {
                    "Показано: $shown из $total"
                } else {
                    "Всего: $total"
                },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (state.participants.isEmpty() && !state.isLoading) {
                Text(
                    "Участников пока нет",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else if (visibleParticipants.isEmpty()) {
                Text(
                    "Никто не подходит под запрос",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(visibleParticipants, key = { it.userId }) { p ->
                        val canManage = state.isOrganizer &&
                            !p.role.equals("ORGANIZER", ignoreCase = true) &&
                            p.userId != state.currentUserId

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    enabled = !state.actionInProgress
                                ) { onOpenUserProfile(p.userId) }
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(
                                    participantDisplayTitle(p),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    p.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    participationRoleRu(p.role),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                if (canManage) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box {
                                            TextButton(
                                                onClick = {
                                                    roleMenuUserId =
                                                        if (roleMenuUserId == p.userId) null else p.userId
                                                },
                                                enabled = !state.actionInProgress
                                            ) {
                                                Text("Сменить роль")
                                            }
                                            DropdownMenu(
                                                expanded = roleMenuUserId == p.userId,
                                                onDismissRequest = { roleMenuUserId = null }
                                            ) {
                                                if (!p.role.equals("PERFORMER", ignoreCase = true)) {
                                                    DropdownMenuItem(
                                                        text = { Text(participationRoleRu("PERFORMER")) },
                                                        onClick = {
                                                            roleMenuUserId = null
                                                            viewModel.changeParticipantRole(
                                                                p.userId,
                                                                "PERFORMER"
                                                            )
                                                        }
                                                    )
                                                }
                                                if (!p.role.equals("COORDINATOR", ignoreCase = true)) {
                                                    DropdownMenuItem(
                                                        text = { Text(participationRoleRu("COORDINATOR")) },
                                                        onClick = {
                                                            roleMenuUserId = null
                                                            viewModel.changeParticipantRole(
                                                                p.userId,
                                                                "COORDINATOR"
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        TextButton(
                                            onClick = { pendingRemove = p },
                                            enabled = !state.actionInProgress
                                        ) {
                                            Text(
                                                "Исключить",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
