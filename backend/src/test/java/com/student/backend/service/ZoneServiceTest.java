package com.student.backend.service;

import com.student.backend.dto.ZoneCreateRequest;
import com.student.backend.exception.AccessDeniedException;
import com.student.backend.model.EventStatus;
import com.student.backend.model.FullName;
import com.student.backend.model.Event;
import com.student.backend.model.Participation;
import com.student.backend.model.User;
import com.student.backend.model.UserRole;
import java.time.LocalDateTime;
import com.student.backend.repository.EventRepository;
import com.student.backend.repository.ParticipationRepository;
import com.student.backend.repository.UserRepository;
import com.student.backend.repository.ZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock private ZoneRepository zoneRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private ParticipationRepository participationRepository;

    @InjectMocks
    private ZoneService zoneService;

    @Test
    void createZone_performerRole_denied() {
        User organizer = new User("org@test.local", "x", new FullName("A", "B", null));
        organizer.setId("org-id");
        Event event = new Event(
                "E", "addr", 0, EventStatus.PLANNED,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                organizer
        );
        event.setId("e1");
        User user = new User("perf@test.local", "x", new FullName("P", "P", null));
        user.setId("u-perf");
        Participation participation = new Participation(user, event, UserRole.PERFORMER);

        when(eventRepository.findById("e1")).thenReturn(Optional.of(event));
        when(userRepository.findById("u-perf")).thenReturn(Optional.of(user));
        when(participationRepository.findByUserIdAndEventId("u-perf", "e1"))
                .thenReturn(Optional.of(participation));

        ZoneCreateRequest req = new ZoneCreateRequest("Зона", null, 0, "e1");

        assertThatThrownBy(() -> zoneService.createZone(req, "u-perf"))
                .isInstanceOf(AccessDeniedException.class);

        verify(zoneRepository, never()).save(any());
    }
}
