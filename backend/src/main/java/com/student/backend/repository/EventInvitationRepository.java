package com.student.backend.repository;

import com.student.backend.model.EventInvitation;
import com.student.backend.model.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventInvitationRepository extends JpaRepository<EventInvitation, String> {
    List<EventInvitation> findByInvitedUserIdAndStatus(String invitedUserId, InvitationStatus status);
    List<EventInvitation> findByEventIdAndInvitedUserIdAndStatus(String eventId, String invitedUserId, InvitationStatus status);
    Optional<EventInvitation> findFirstByEventIdAndInvitedUserIdAndStatus(String eventId, String invitedUserId, InvitationStatus status);
}
