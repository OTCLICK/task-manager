package com.example.mobile.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mobile.data.model.User
import com.example.mobile.presentation.participationRoleRu
import com.example.mobile.presentation.viewmodel.EventInviteViewModel

private val inviteRoleOptions = listOf("PERFORMER", "COORDINATOR")

private fun User.displayName(): String {
    val fn = fullName
    val p = fn.patronymic?.takeIf { it.isNotBlank() }
    return listOf(fn.surname, fn.name, p)
        .filterNotNull()
        .filter { it.isNotBlank() }
        .joinToString(" ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventInviteScreen(
    viewModel: EventInviteViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.eventTitle?.let { "Приглашение · $it" } ?: "Поиск и приглашение") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
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
            Text(
                "Поиск по имени или фамилии (не менее 2 символов). Приглашение уходит на email пользователя.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Поиск пользователя") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = null)
                    }
                )
            }

            if (state.isSearchLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Text("Роль в мероприятии", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                inviteRoleOptions.forEach { role ->
                    FilterChip(
                        selected = state.inviteRole == role,
                        onClick = { viewModel.setInviteRole(role) },
                        label = { Text(participationRoleRu(role)) }
                    )
                }
            }

            state.errorMessage?.let { err ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.clearMessages() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        err,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            state.successMessage?.let { ok ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.clearMessages() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        ok,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text("Результаты поиска", style = MaterialTheme.typography.titleSmall)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                val q = state.searchQuery.trim()
                if (q.length >= 2 && !state.isSearchLoading && state.searchResults.isEmpty() && state.errorMessage == null) {
                    item {
                        Text(
                            "Пользователи не найдены",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                items(state.searchResults, key = { it.id }) { user ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    user.displayName(),
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TextButton(
                                onClick = { viewModel.inviteByEmail(user.email) { } },
                                enabled = !state.isInviteLoading
                            ) { Text("Пригласить") }
                        }
                    }
                }
            }

            Text("Или введите email вручную", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = state.manualEmail,
                onValueChange = { viewModel.onManualEmailChange(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Email") },
                placeholder = { Text("user@example.com") }
            )
            TextButton(
                onClick = { viewModel.inviteByEmail(state.manualEmail) { } },
                modifier = Modifier.align(Alignment.End),
                enabled = !state.isInviteLoading && state.manualEmail.isNotBlank()
            ) {
                Text("Отправить приглашение")
            }
        }
    }
}
