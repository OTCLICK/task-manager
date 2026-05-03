package com.student.backend.repository;

import com.student.backend.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task,String> {

    List<Task> findAll();

    Optional<Task> findById(String id);

    void deleteById(String id);

    List<Task> findByTitle(String title);

    List<Task> findByPriority(TaskPriority priority);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByZone(Zone zone);

    List<Task> findByZoneId(String zoneId);

    List<Task> findByPerformers(User performer);

    List<Task> findByPerformersId(String performerId);

    List<Task> findByCoordinator(User coordinator);

    List<Task> findByCoordinatorId(String coordinatorId);

    List<Task> findByDeadline(LocalDateTime deadline);

}
