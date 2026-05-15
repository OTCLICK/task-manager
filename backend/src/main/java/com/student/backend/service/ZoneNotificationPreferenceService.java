package com.student.backend.service;

import com.student.backend.exception.NotFoundException;
import com.student.backend.model.User;
import com.student.backend.model.UserZoneNotificationMute;
import com.student.backend.model.Zone;
import com.student.backend.repository.UserRepository;
import com.student.backend.repository.UserZoneNotificationMuteRepository;
import com.student.backend.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ZoneNotificationPreferenceService {

    private final UserZoneNotificationMuteRepository muteRepository;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;

    public ZoneNotificationPreferenceService(
            UserZoneNotificationMuteRepository muteRepository,
            UserRepository userRepository,
            ZoneRepository zoneRepository
    ) {
        this.muteRepository = muteRepository;
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
    }

    @Transactional(readOnly = true)
    public List<String> getMutedZoneIds(String userId) {
        return muteRepository.findByUser_Id(userId).stream()
                .map(m -> m.getZone().getId())
                .collect(Collectors.toList());
    }

    public void replaceMutedZones(String userId, List<String> zoneIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        muteRepository.deleteAllByUserId(userId);
        if (zoneIds == null || zoneIds.isEmpty()) {
            return;
        }
        for (String zid : zoneIds.stream().distinct().toList()) {
            Zone zone = zoneRepository.findById(zid)
                    .orElseThrow(() -> new NotFoundException("Зона не найдена: " + zid));
            muteRepository.save(new UserZoneNotificationMute(user, zone));
        }
    }
}
