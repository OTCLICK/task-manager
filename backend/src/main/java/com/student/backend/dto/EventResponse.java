package com.student.backend.dto;

import com.student.backend.model.EventStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public class EventResponse {
    private final String eventId;
    private final String name;
    private final String address;
    private final int participatesCount;
    private final EventStatus status;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final UserResponse organizer;

    public EventResponse(String eventId, String name, String address, int participatesCount, EventStatus status,
                         LocalDateTime startTime, LocalDateTime endTime, UserResponse organizer) {
        this.eventId = eventId;
        this.name = name;
        this.address = address;
        this.participatesCount = participatesCount;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizer = organizer;
    }

    public String getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getParticipatesCount() {
        return participatesCount;
    }

    public EventStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public UserResponse getOrganizer() {
        return organizer;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EventResponse that = (EventResponse) o;
        return participatesCount == that.participatesCount && Objects.equals(eventId, that.eventId) && Objects.equals(name, that.name) && Objects.equals(address, that.address) && status == that.status && Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime) && Objects.equals(organizer, that.organizer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, name, address, participatesCount, status, startTime, endTime, organizer);
    }
}
