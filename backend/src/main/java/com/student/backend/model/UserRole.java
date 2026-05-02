package com.student.backend.model;

public enum UserRole {
    ORGANIZER(0),
    COORDINATOR(1),
    PERFORMER(2),
    ADMIN(3);

    private final int VALUE;

    UserRole(int value) {
        this.VALUE = value;
    }

    public int getValue() {
        return VALUE;
    }
}
