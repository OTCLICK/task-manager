package com.student.backend.controller;

import com.student.backend.dto.FcmTokenRequest;
import com.student.backend.security.SecurityUtils;
import com.student.backend.service.PushNotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class FcmTokenController {

    private final PushNotificationService pushNotificationService;
    private final SecurityUtils securityUtils;

    public FcmTokenController(PushNotificationService pushNotificationService, SecurityUtils securityUtils) {
        this.pushNotificationService = pushNotificationService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<Void> registerFcmToken(@Valid @RequestBody FcmTokenRequest request) {
        String userId = securityUtils.getCurrentUserId();
        pushNotificationService.registerToken(userId, request.token());
        return ResponseEntity.ok().build();
    }

    /**
     * Удаление токена устройства. Если query {@code token} не передан — удаляются все токены пользователя (выход со всех устройств в приложении).
     */
    @DeleteMapping("/fcm-token")
    public ResponseEntity<Void> unregisterFcmToken(@RequestParam(required = false) String token) {
        String userId = securityUtils.getCurrentUserId();
        pushNotificationService.removeToken(userId, token);
        return ResponseEntity.noContent().build();
    }
}
