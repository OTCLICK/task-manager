package com.example.mobile.domain

import com.example.mobile.data.model.EventCreateRequest
import com.example.mobile.data.repository.EventRepository

class CreateEventUseCase(private val eventRepository: EventRepository) {
    suspend operator fun invoke(request: EventCreateRequest): Result<Unit> {
        return eventRepository.createEvent(request)
    }
}