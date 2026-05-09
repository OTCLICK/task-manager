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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobile.presentation.viewmodel.InvitationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationScreen(
    eventId: String,
    viewModel: InvitationViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadInvitations(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои приглашения") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Назад") }
                },
                actions = {
                    TextButton(onClick = { viewModel.loadInvitations(eventId) }) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
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
                                viewModel.loadInvitations(eventId)
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) { Text("Повторить") }
                    }
                }
            }

            if (state.invitations.isEmpty() && !state.isLoading) {
                Text("Активных приглашений для этого мероприятия нет.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.invitations, key = { it.invitationId }) { invitation ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(invitation.eventName, style = MaterialTheme.typography.titleMedium)
                                Text("Пригласил: ${invitation.invitedByEmail}", style = MaterialTheme.typography.bodySmall)
                                Text("Предложенная роль: ${invitation.role}", style = MaterialTheme.typography.bodySmall)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.acceptInvitation(eventId, invitation.invitationId)
                                        }
                                    ) {
                                        Text("Принять")
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.declineInvitation(eventId, invitation.invitationId)
                                        }
                                    ) {
                                        Text("Отклонить")
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
