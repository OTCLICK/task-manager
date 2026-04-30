package com.student.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "event")
public class Event extends BaseEntity {
    private String name;

    private String address;

    private int participatesCount;

    private EventStatus status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private User organizer;

    protected Event() {}

    public Event(String name, String address, EventStatus status, LocalDateTime startTime, LocalDateTime endTime, User organizer) {
        this.name = name;
        this.address = address;
        this.status = status != null ? status : EventStatus.PLANNED;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizer = organizer;
    }

    @NotBlank
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotBlank
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getParticipatesCount() {
        return participatesCount;
    }

    public void setParticipatesCount(int participatesCount) {
        this.participatesCount = participatesCount;
    }

    @Enumerated(EnumType.STRING)
    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

}
