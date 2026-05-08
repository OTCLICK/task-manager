package com.student.backend.dto;

import com.student.backend.model.UserRole;
import org.springframework.hateoas.server.core.Relation;

import java.util.Objects;

@Relation(collectionRelation = "users", itemRelation = "user")
public class UserResponse {
    private final String userId;
    private final String email;
    private final FullNameDto fullName;
//    private final UserRole userRole;

    public UserResponse(String userId, String email, FullNameDto fullName/*, UserRole userRole*/) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
//        this.userRole = userRole;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public FullNameDto getFullName() {
        return fullName;
    }

//    public UserRole getUserRole() {
//        return userRole;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (o == null || getClass() != o.getClass()) return false;
//        UserResponse that = (UserResponse) o;
//        return Objects.equals(userId, that.userId) && Objects.equals(email, that.email) && Objects.equals(fullName, that.fullName) && userRole == that.userRole;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(userId, email, fullName, userRole);
//    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserResponse that = (UserResponse) o;
        return Objects.equals(userId, that.userId) && Objects.equals(email, that.email) && Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, fullName);
    }
}
