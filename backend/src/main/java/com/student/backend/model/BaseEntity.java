package com.student.backend.model;

import jakarta.persistence.*;

//сервисы
//контроллеры
//мапперы
//exception handlers
//auth
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
