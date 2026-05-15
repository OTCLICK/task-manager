package com.student.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirebaseConfiguration {

    @Bean
    public FirebaseMessaging firebaseMessaging(
            ResourceLoader resourceLoader,
            @Value("${firebase.credentials-json:classpath:firebase-service-account.json}") String location
    ) throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException(
                        "firebase.enabled=true, но файл учётных данных не найден: " + location
                );
            }
            try (InputStream in = resource.getInputStream()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(in);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                FirebaseApp.initializeApp(options);
            }
        }
        return FirebaseMessaging.getInstance();
    }
}