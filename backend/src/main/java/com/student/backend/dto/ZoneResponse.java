package com.student.backend.dto;

import java.util.Objects;

public class ZoneResponse {
    private final String zoneId;
    private final String name;
    private final int participatesCount;
    private final EventResponse event;
    private final UserResponse user;

    public ZoneResponse(String zoneId, String name, int participatesCount, EventResponse event, UserResponse user) {
        this.zoneId = zoneId;
        this.name = name;
        this.participatesCount = participatesCount;
        this.event = event;
        this.user = user;
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getName() {
        return name;
    }

    public int getParticipatesCount() {
        return participatesCount;
    }

    public EventResponse getEvent() {
        return event;
    }

    public UserResponse getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ZoneResponse that = (ZoneResponse) o;
        return participatesCount == that.participatesCount && Objects.equals(zoneId, that.zoneId) && Objects.equals(name, that.name) && Objects.equals(event, that.event) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zoneId, name, participatesCount, event, user);
    }
}
