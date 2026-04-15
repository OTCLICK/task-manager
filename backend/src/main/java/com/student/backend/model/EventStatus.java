package com.student.backend.model;

public enum EventStatus {
    PLANNED(0),
    ACTIVE(1),
    COMPLETED(2),
    CANCELLED(3);

    private final int VALUE;

    EventStatus(int value) {
        this.VALUE = value;
    }

    public int getValue() {
        return VALUE;
    }
}
