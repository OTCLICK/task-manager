package com.student.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    private String email;

    private String password;

    private FullName fullName;

//    private UserRole role;

    protected User() {}

    public User(String email, String password, FullName fullName/*, UserRole role*/) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
//        this.role = role;
    }

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    public UserRole getRole() {
//        return role;
//    }
//
//    public void setRole(UserRole role) {
//        this.role = role;
//    }

    @Embedded
    public FullName getFullName() {
        return fullName;
    }

    public void setFullName(FullName fullName) {
        this.fullName = fullName;
    }

    @NotBlank
    @Column(nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
