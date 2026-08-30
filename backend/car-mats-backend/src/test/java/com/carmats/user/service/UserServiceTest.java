package com.carmats.user.service;

import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.user.dto.request.ChangePasswordRequest;
import com.carmats.user.dto.request.UpdateProfileRequest;
import com.carmats.user.dto.response.UserResponse;
import com.carmats.user.entity.User;
import com.carmats.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User("user@carmats.local", "$2a$10$oldhash", "Ahmet", "Yılmaz", "+905551112233");
    }

    @Test
    @DisplayName("Should get user profile by id")
    void shouldGetProfile() {
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getProfile(userId);

        assertThat(response.email()).isEqualTo("user@carmats.local");
        assertThat(response.firstName()).isEqualTo("Ahmet");
    }

    @Test
    @DisplayName("Should throw NotFoundException when user does not exist")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(userId))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "USER_NOT_FOUND");
    }

    @Test
    @DisplayName("Should update user profile")
    void shouldUpdateProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest("Ali", "Kaya", "+905559998877");
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.updateProfile(userId, request);

        assertThat(response.firstName()).isEqualTo("Ali");
        assertThat(response.lastName()).isEqualTo("Kaya");
        assertThat(response.phoneNumber()).isEqualTo("+905559998877");
    }

    @Test
    @DisplayName("Should change password with valid current password")
    void shouldChangePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPassword123!", "NewPassword123!");
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123!", "$2a$10$oldhash")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("$2a$10$newhash");

        userService.changePassword(userId, request);

        assertThat(testUser.getPasswordHash()).isEqualTo("$2a$10$newhash");
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw BusinessException when current password does not match")
    void shouldThrowWhenCurrentPasswordIncorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest("WrongPassword!", "NewPassword123!");
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword!", "$2a$10$oldhash")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_CURRENT_PASSWORD");
    }
}
