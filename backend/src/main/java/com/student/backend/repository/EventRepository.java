package com.student.backend.repository;

import com.student.backend.model.Event;
import com.student.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, String> {

    List<Event> findAll();

    Optional<Event> findById(String id);

    List<Event> findByName(String name);

    List<Event> findByOrganizer(User organizer);

    List<Event> findByOrganizerId(String organizerId);

    void deleteById(String id);

//    Optional<Event> findByZoneId(String zoneId);

}
