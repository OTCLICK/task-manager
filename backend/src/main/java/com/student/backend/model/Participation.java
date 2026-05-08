package com.student.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "participation")
public class Participation extends BaseEntity {

    private User user;

    private Event event;

    private UserRole role;

    protected Participation() {}

    public Participation(User user, Event event, UserRole role) {
        this.user = user;
        this.event = event;
        this.role = role;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    public Event getEvent() { return event; }

    public void setEvent(Event event) { this.event = event; }

    @Column(name = "role", nullable = false)
    public UserRole getRole() { return role; }

    public void setRole(UserRole roleName) { this.role = roleName; }
}