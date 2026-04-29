package com.student.backend.repository;

import com.student.backend.model.FullName;
import com.student.backend.model.User;
import com.student.backend.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    List<User> findAll();

    Optional<User> findById(String id);

    void deleteById(String id);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullName.surname) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "(u.fullName.patronymic IS NOT NULL AND LOWER(u.fullName.patronymic) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchByFullNameSubstring(@Param("query") String query);

    List<User> findByRole(UserRole role);

}
