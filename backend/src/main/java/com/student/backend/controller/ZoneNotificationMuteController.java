package com.student.backend.controller;

import com.student.backend.dto.ZoneMuteIdsRequest;
import com.student.backend.security.SecurityUtils;
import com.student.backend.service.ZoneNotificationPreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/zone-notification-mutes")
public class ZoneNotificationMuteController {

    private final ZoneNotificationPreferenceService preferenceService;
    private final SecurityUtils securityUtils;

    public ZoneNotificationMuteController(
            ZoneNotificationPreferenceService preferenceService,
            SecurityUtils securityUtils
    ) {
        this.preferenceService = preferenceService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<List<String>> getMutedZoneIds() {
        String userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(preferenceService.getMutedZoneIds(userId));
    }

    @PutMapping
    public ResponseEntity<Void> setMutedZones(@Valid @RequestBody ZoneMuteIdsRequest request) {
        String userId = securityUtils.getCurrentUserId();
        preferenceService.replaceMutedZones(userId, request.mutedZoneIds());
        return ResponseEntity.noContent().build();
    }
}
