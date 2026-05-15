package com.student.backend.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_fcm_tokens", uniqueConstraints = {
        @UniqueConstraint(name = "uk_fcm_token", columnNames = "token")
})
public class UserFcmToken extends BaseEntity {

    private User user;
    private String token;
    private Instant updatedAt;

    protected UserFcmToken() {}

    public UserFcmToken(User user, String token, Instant updatedAt) {
        this.user = user;
        this.token = token;
        this.updatedAt = updatedAt;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(nullable = false, length = 512)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Column(nullable = false)
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
