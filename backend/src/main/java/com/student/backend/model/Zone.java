package com.student.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "zone")
public class Zone extends BaseEntity {

    private String name;

    private String description;

    private int participatesCount;

    private Event event;

    private User coordinator;

    protected Zone() {}

    public Zone(String name, String description, Event event, User coordinator) {
        this.name = name;
        this.description = description;
        this.event = event;
        this.coordinator = coordinator;
    }

    @NotBlank
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getParticipatesCount() {
        return participatesCount;
    }

    public void setParticipatesCount(int participatesCount) {
        this.participatesCount = participatesCount;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id", nullable = false)
    public User getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(User coordinator) {
        this.coordinator = coordinator;
    }
}
