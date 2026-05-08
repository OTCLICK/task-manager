package com.student.backend.repository;

import com.student.backend.model.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, String> {
    List<Participation> findByUserId(String userId);
    List<Participation> findByEventId(String eventId);
    Optional<Participation> findByUserIdAndEventId(String userId, String eventId);
}