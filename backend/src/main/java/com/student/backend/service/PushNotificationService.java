package com.student.backend.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.student.backend.exception.NotFoundException;
import com.student.backend.model.User;
import com.student.backend.model.UserFcmToken;
import com.student.backend.repository.UserFcmTokenRepository;
import com.student.backend.repository.UserRepository;
import com.student.backend.repository.UserZoneNotificationMuteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final UserFcmTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserZoneNotificationMuteRepository zoneMuteRepository;
    private final FirebaseMessaging firebaseMessaging;

    public PushNotificationService(
            UserFcmTokenRepository tokenRepository,
            UserRepository userRepository,
            UserZoneNotificationMuteRepository zoneMuteRepository,
            @Autowired(required = false) FirebaseMessaging firebaseMessaging
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.zoneMuteRepository = zoneMuteRepository;
        this.firebaseMessaging = firebaseMessaging;
    }

    @Transactional
    public void registerToken(String userId, String token) {
        String trimmed = token.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        tokenRepository.deleteByToken(trimmed);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        UserFcmToken row = new UserFcmToken(user, trimmed, Instant.now());
        tokenRepository.save(row);
    }

    @Transactional
    public void removeToken(String userId, String token) {
        if (token == null || token.isBlank()) {
            tokenRepository.deleteAllByUserId(userId);
        } else {
            tokenRepository.deleteByUserIdAndToken(userId, token.trim());
        }
    }

    /**
     * Отправка push всем зарегистрированным устройствам пользователя.
     * Если Firebase не настроен ({@code firebase.enabled=false}), метод ничего не делает.
     */
    public void sendToUser(String userId, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            return;
        }
        try {
            sendToUserInternal(userId, title, body, data);
        } catch (Exception e) {
            log.warn("Не удалось отправить push пользователю {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Push с учётом настройки «не уведомлять о зоне»: при непустом {@code zoneId}, если у пользователя
     * включён mute для этой зоны, отправка пропускается.
     */
    public void sendToUserRespectingZoneMute(String userId, String zoneId, String title, String body, Map<String, String> data) {
        if (firebaseMessaging == null) {
            return;
        }
        if (zoneId != null && !zoneId.isBlank() && zoneMuteRepository.existsByUser_IdAndZone_Id(userId, zoneId)) {
            return;
        }
        sendToUser(userId, title, body, data);
    }

    private void sendToUserInternal(String userId, String title, String body, Map<String, String> data) throws FirebaseMessagingException {
        List<String> tokens = tokenRepository.findByUser_Id(userId).stream()
                .map(UserFcmToken::getToken)
                .toList();
        if (tokens.isEmpty()) {
            return;
        }
        Map<String, String> payload = data != null ? new HashMap<>(data) : new HashMap<>();
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        int batchSize = 500;
        for (int i = 0; i < tokens.size(); i += batchSize) {
            List<String> chunk = tokens.subList(i, Math.min(i + batchSize, tokens.size()));
            MulticastMessage.Builder mb = MulticastMessage.builder()
                    .addAllTokens(chunk)
                    .setNotification(notification);
            payload.forEach(mb::putData);
            try {
                BatchResponse response = firebaseMessaging.sendEachForMulticast(mb.build());
                List<com.google.firebase.messaging.SendResponse> responses = response.getResponses();
                for (int j = 0; j < responses.size(); j++) {
                    if (!responses.get(j).isSuccessful()) {
                        FirebaseMessagingException ex = responses.get(j).getException();
                        if (ex != null && isInvalidOrUnregistered(ex)) {
                            tokenRepository.deleteByToken(chunk.get(j));
                        }
                        if (ex != null) {
                            log.debug("FCM send failed for token: {}", ex.getMessage());
                        }
                    }
                }
            } catch (FirebaseMessagingException e) {
                log.warn("FCM multicast failed: {}", e.getMessage());
            }
        }
    }

    private static boolean isInvalidOrUnregistered(FirebaseMessagingException ex) {
        MessagingErrorCode code = ex.getMessagingErrorCode();
        return code == MessagingErrorCode.UNREGISTERED
                || code == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
