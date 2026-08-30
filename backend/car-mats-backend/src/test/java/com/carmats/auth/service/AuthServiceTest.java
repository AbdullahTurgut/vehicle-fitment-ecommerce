package com.carmats.auth.service;

import com.carmats.auth.dto.request.LoginRequest;
import com.carmats.auth.dto.request.RefreshTokenRequest;
import com.carmats.auth.dto.request.RegisterRequest;
import com.carmats.auth.dto.response.AuthResponse;
import com.carmats.auth.entity.RefreshToken;
import com.carmats.auth.repository.RefreshTokenRepository;
import com.carmats.common.exception.BusinessException;
import com.carmats.config.security.CustomUserDetails;
import com.carmats.config.security.JwtService;
import com.carmats.user.dto.response.UserResponse;
import com.carmats.user.entity.Role;
import com.carmats.user.entity.User;
import com.carmats.user.repository.RoleRepository;
import com.carmats.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Role customerRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        customerRole = new Role(Role.ROLE_CUSTOMER, "Customer");
        testUser = new User("test@carmats.local", "$2a$10$hashed", "Ahmet", "Yılmaz", "+905551112233");
        testUser.addRole(customerRole);
    }

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUser() {
        RegisterRequest request = new RegisterRequest(
                "newuser@carmats.local",
                "Password123!",
                "Mehmet",
                "Demir",
                "+905552223344"
        );

        when(userRepository.existsByEmail("newuser@carmats.local")).thenReturn(false);
        when(roleRepository.findByName(Role.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("jwt-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(86400000L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("newuser@carmats.local");
        assertThat(response.user().firstName()).isEqualTo("Mehmet");
        assertThat(response.user().roles()).contains(Role.ROLE_CUSTOMER);
    }

    @Test
    @DisplayName("Should throw BusinessException when registering existing email")
    void shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "existing@carmats.local",
                "Password123!",
                "Mehmet",
                "Demir",
                null
        );

        when(userRepository.existsByEmail("existing@carmats.local")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "EMAIL_ALREADY_EXISTS");
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("test@carmats.local", "Password123!");

        when(userRepository.findByEmailWithRoles("test@carmats.local")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "$2a$10$hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("login-jwt-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(86400000L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("login-jwt-token");
        assertThat(response.user().email()).isEqualTo("test@carmats.local");
    }

    @Test
    @DisplayName("Should throw BusinessException on invalid login credentials")
    void shouldThrowOnInvalidCredentials() {
        LoginRequest request = new LoginRequest("test@carmats.local", "WrongPassword");

        when(userRepository.findByEmailWithRoles("test@carmats.local")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_CREDENTIALS");
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        RefreshToken refreshToken = new RefreshToken(testUser, "valid-refresh-token", LocalDateTime.now().plusDays(7));
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        when(refreshTokenRepository.findByTokenWithUser("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("refreshed-jwt-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(86400000L);
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);

        AuthResponse response = authService.refreshToken(request);

        assertThat(response.accessToken()).isEqualTo("refreshed-jwt-token");
        assertThat(refreshToken.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should get current user by id")
    void shouldGetCurrentUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(testUser));

        UserResponse response = authService.getCurrentUser(userId);

        assertThat(response.email()).isEqualTo("test@carmats.local");
        assertThat(response.roles()).contains(Role.ROLE_CUSTOMER);
    }
}
