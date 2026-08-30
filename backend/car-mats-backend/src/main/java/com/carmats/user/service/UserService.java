package com.carmats.user.service;

import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.user.dto.request.ChangePasswordRequest;
import com.carmats.user.dto.request.UpdateProfileRequest;
import com.carmats.user.dto.response.UserResponse;
import com.carmats.user.entity.User;
import com.carmats.user.mapper.UserMapper;
import com.carmats.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        User user = findUserById(userId);
        return UserMapper.toUserResponse(user);
    }

    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhoneNumber(request.phoneNumber() != null ? request.phoneNumber().trim() : null);

        User updated = userRepository.save(user);
        return UserMapper.toUserResponse(updated);
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(
                    "INVALID_CURRENT_PASSWORD",
                    "Mevcut şifreniz hatalı."
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User findUserById(UUID userId) {
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "USER_NOT_FOUND",
                                "Kullanıcı bulunamadı."
                        )
                );
    }
}
