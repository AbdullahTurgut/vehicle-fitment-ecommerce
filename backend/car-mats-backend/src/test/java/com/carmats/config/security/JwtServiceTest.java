package com.carmats.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        properties.setAccessTokenExpirationMs(3600000); // 1 hour
        properties.setRefreshTokenExpirationMs(86400000); // 24 hours

        jwtService = new JwtService(properties);
    }

    @Test
    @DisplayName("Should generate and validate JWT token")
    void shouldGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = new CustomUserDetails(
                userId,
                "user@example.com",
                "secret",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        String token = jwtService.generateAccessToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("user@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractRoles(token)).containsExactly("ROLE_CUSTOMER");
    }

    @Test
    @DisplayName("Should return false when token is invalid for another username")
    void shouldReturnFalseForDifferentUser() {
        CustomUserDetails user1 = new CustomUserDetails(
                UUID.randomUUID(),
                "user1@example.com",
                "secret",
                true,
                List.of()
        );
        CustomUserDetails user2 = new CustomUserDetails(
                UUID.randomUUID(),
                "user2@example.com",
                "secret",
                true,
                List.of()
        );

        String token = jwtService.generateAccessToken(user1);

        assertThat(jwtService.isTokenValid(token, user2)).isFalse();
    }
}
