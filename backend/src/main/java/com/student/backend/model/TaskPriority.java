package com.student.backend.model;

public enum TaskPriority {
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    private final int VALUE;

    TaskPriority(int value) {
        this.VALUE = value;
    }

    public int getValue() {
        return VALUE;
    }
}
