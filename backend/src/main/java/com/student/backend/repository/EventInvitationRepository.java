package com.student.backend.repository;

import com.student.backend.model.EventInvitation;
import com.student.backend.model.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventInvitationRepository extends JpaRepository<EventInvitation, String> {
    List<EventInvitation> findByInvitedUserIdAndStatus(String invitedUserId, InvitationStatus status);

    List<EventInvitation> findByInvitedByIdOrderByCreatedAtDesc(String invitedByUserId);

    List<EventInvitation> findByEventIdAndInvitedUserIdAndStatus(String eventId, String invitedUserId, InvitationStatus status);

    Optional<EventInvitation> findFirstByEventIdAndInvitedUserIdAndStatus(String eventId, String invitedUserId, InvitationStatus status);

    @Query("SELECT i FROM EventInvitation i WHERE i.event.id = :eventId AND i.invitedBy.id = :invitedById AND i.status = :status")
    List<EventInvitation> findPendingOutboundForEventAndInviter(
            @Param("eventId") String eventId,
            @Param("invitedById") String invitedById,
            @Param("status") InvitationStatus status
    );
}
