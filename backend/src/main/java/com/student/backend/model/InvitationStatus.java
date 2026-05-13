package com.student.backend.model;

public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    /** Отозвано отправителем до ответа адресата. */
    CANCELLED
}
