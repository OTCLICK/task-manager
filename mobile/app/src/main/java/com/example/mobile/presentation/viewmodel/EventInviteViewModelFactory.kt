package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobile.data.repository.EventWorkspaceRepository

class EventInviteViewModelFactory(
    private val repository: EventWorkspaceRepository,
    private val eventId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventInviteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventInviteViewModel(repository, eventId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
