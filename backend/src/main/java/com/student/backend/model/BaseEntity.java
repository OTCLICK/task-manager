package com.student.backend.model;

import jakarta.persistence.*;

// Логика вхождения в мероприятие (роли)
@MappedSuperclass
public abstract class BaseEntity {
    private String id;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
