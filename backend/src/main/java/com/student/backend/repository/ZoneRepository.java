package com.student.backend.repository;

import com.student.backend.model.Event;
import com.student.backend.model.User;
import com.student.backend.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, String> {

    List<Zone> findAll();

    Optional<Zone> findById(String id);

    void deleteById(String id);

    List<Zone> findByName(String name);

    List<Zone> findByEvent(Event event);

    List<Zone> findByEventId(String eventId);

    List<Zone> findByCoordinator(User coordinator);

    List<Zone> findByCoordinatorId(User coordinatorId);

}