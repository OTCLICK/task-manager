package com.student.backend.service;

import com.student.backend.exception.NotFoundException;
import com.student.backend.model.User;
import com.student.backend.model.UserZoneNotificationMute;
import com.student.backend.model.Zone;
import com.student.backend.repository.UserRepository;
import com.student.backend.repository.UserZoneNotificationMuteRepository;
import com.student.backend.repository.ZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZoneNotificationPreferenceServiceTest {

    @Mock
    private UserZoneNotificationMuteRepository muteRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ZoneRepository zoneRepository;

    @InjectMocks
    private ZoneNotificationPreferenceService service;

    @Test
    void getMutedZoneIds_returnsZoneIds() {
        User user = org.mockito.Mockito.mock(User.class);
        Zone zone = org.mockito.Mockito.mock(Zone.class);
        when(zone.getId()).thenReturn("z1");
        UserZoneNotificationMute mute = new UserZoneNotificationMute(user, zone);

        when(muteRepository.findByUser_Id("u1")).thenReturn(List.of(mute));

        assertThat(service.getMutedZoneIds("u1")).containsExactly("z1");
    }

    @Test
    void replaceMutedZones_clearsAndSavesDistinctZones() {
        User user = org.mockito.Mockito.mock(User.class);
        Zone z1 = org.mockito.Mockito.mock(Zone.class);
        Zone z2 = org.mockito.Mockito.mock(Zone.class);
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(zoneRepository.findById("z1")).thenReturn(Optional.of(z1));
        when(zoneRepository.findById("z2")).thenReturn(Optional.of(z2));

        service.replaceMutedZones("u1", List.of("z1", "z1", "z2"));

        verify(muteRepository).deleteAllByUserId("u1");
        verify(muteRepository, org.mockito.Mockito.times(2)).save(any(UserZoneNotificationMute.class));
    }

    @Test
    void replaceMutedZones_unknownZone_throwsNotFound() {
        User user = org.mockito.Mockito.mock(User.class);
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(zoneRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.replaceMutedZones("u1", List.of("missing")))
                .isInstanceOf(NotFoundException.class);
    }
}
