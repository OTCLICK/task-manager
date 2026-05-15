package com.student.backend.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "user_zone_notification_mutes",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_zone_mute", columnNames = {"user_id", "zone_id"})
)
public class UserZoneNotificationMute extends BaseEntity {

    private User user;
    private Zone zone;

    protected UserZoneNotificationMute() {}

    public UserZoneNotificationMute(User user, Zone zone) {
        this.user = user;
        this.zone = zone;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }
}
