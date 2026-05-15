package com.student.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Полный список id зон, по которым пользователь не хочет получать push о задачах. */
public record ZoneMuteIdsRequest(
        @NotNull
        List<String> mutedZoneIds
) {}
