package com.student.backend.controller;

import com.student.backend.dto.ZoneCreateRequest;
import com.student.backend.dto.ZoneResponse;
import com.student.backend.security.SecurityUtils;
import com.student.backend.service.ZoneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZoneService zoneService;
    private final SecurityUtils securityUtils;

    public ZoneController(ZoneService zoneService, SecurityUtils securityUtils) {
        this.zoneService = zoneService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<List<ZoneResponse>> getAllZones() {
        List<ZoneResponse> zones = zoneService.getAllZones();
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneResponse> getZoneById(@PathVariable String id) {
        ZoneResponse zone = zoneService.getZoneById(id);
        return ResponseEntity.ok(zone);
    }

    @PostMapping
    public ResponseEntity<ZoneResponse> createZone(
            @Valid @RequestBody ZoneCreateRequest request
    ) {
        String coordinatorId = securityUtils.getCurrentUserId();
        ZoneResponse zone = zoneService.createZone(request, request.eventId(), coordinatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(zone);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable String id) {
        String coordinatorId = securityUtils.getCurrentUserId();
        zoneService.deleteZone(id,  coordinatorId);
        return ResponseEntity.noContent().build();
    }
}
