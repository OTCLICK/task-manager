package com.student.backend.model;

public enum TaskStatus {
    CREATED(0),
    IN_PROGRESS(1),
    HELP_REQUESTED(2),
    COMPLETED(3),
    CANCELLED(4);

    private final int VALUE;

    TaskStatus(int value) {
        this.VALUE = value;
    }

    public int getValue() {
        return VALUE;
    }

}
