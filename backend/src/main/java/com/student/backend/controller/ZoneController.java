package com.student.backend.controller;

import com.student.backend.dto.ZoneCreateRequest;
import com.student.backend.dto.ZoneResponse;
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

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
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

    // TODO: Требуется авторизация - только координатор может создавать зоны
    // Временно используем параметры eventId и coordinatorId для тестирования
    @PostMapping
    public ResponseEntity<ZoneResponse> createZone(
            @Valid @RequestBody ZoneCreateRequest request,
            @RequestParam String eventId,
            @RequestParam String coordinatorId
    ) {
        ZoneResponse zone = zoneService.createZone(request, eventId, coordinatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(zone);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable String id) {
        // TODO: Требуется авторизация - только создатель зоны может её удалить
        zoneService.deleteZone(id);
        return ResponseEntity.noContent().build();
    }
}
