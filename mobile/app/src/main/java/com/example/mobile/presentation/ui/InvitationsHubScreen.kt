package com.example.mobile.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobile.presentation.invitationStatusRu
import com.example.mobile.presentation.participationRoleRu
import com.example.mobile.presentation.viewmodel.InvitationsHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationsHubScreen(
    viewModel: InvitationsHubViewModel,
    onBack: () -> Unit,
    onOpenEvent: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Приглашения") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Назад") }
                },
                actions = {
                    TextButton(onClick = { viewModel.refresh() }) { Text("Обновить") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = state.selectedTab) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Ко мне") }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("От меня") }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                if (state.errorMessage != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Ошибка", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = state.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Button(
                                onClick = {
                                    viewModel.clearError()
                                    viewModel.refresh()
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) { Text("Повторить") }
                        }
                    }
                }

                when (state.selectedTab) {
                    0 -> {
                        if (state.incoming.isEmpty() && !state.isLoading) {
                            Text(
                                "Нет входящих приглашений в ожидании",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.incoming, key = { it.invitationId }) { inv ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(inv.eventName, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                "Приглашение от: ${inv.invitedByEmail}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                "Роль: ${participationRoleRu(inv.role)}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.acceptInvitation(inv.eventId, inv.invitationId)
                                                    }
                                                ) { Text("Принять") }
                                                Button(
                                                    onClick = {
                                                        viewModel.declineInvitation(inv.eventId, inv.invitationId)
                                                    }
                                                ) { Text("Отклонить") }
                                            }
                                            TextButton(
                                                onClick = { onOpenEvent(inv.eventId) },
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Text("Открыть мероприятие")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        if (state.sent.isEmpty() && !state.isLoading) {
                            Text(
                                "Вы ещё никого не приглашали (или список пуст)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.sent, key = { it.invitationId }) { inv ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(inv.eventName, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                "Адресат: ${inv.invitedUserEmail}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                "Роль: ${participationRoleRu(inv.role)} · ${invitationStatusRu(inv.status)}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            TextButton(
                                                onClick = { onOpenEvent(inv.eventId) },
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Text("Открыть мероприятие")
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
}
