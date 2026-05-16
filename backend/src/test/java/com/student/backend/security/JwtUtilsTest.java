package com.student.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",
                "c2VjdXJlLWtleS1mb3Itand0LXRva2VuLWdlbmVyYXRpb24=");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3_600_000);
    }

    @Test
    void generateToken_containsUserIdAndEmail() {
        String token = jwtUtils.generateToken("user-123", "test@example.com");

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getUserIdFromToken(token)).isEqualTo("user-123");
        assertThat(jwtUtils.getEmailFromToken(token)).isEqualTo("test@example.com");
    }

    @Test
    void validateToken_rejectsTamperedToken() {
        String token = jwtUtils.generateToken("user-123", "test@example.com");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtUtils.validateToken(tampered)).isFalse();
    }
}
