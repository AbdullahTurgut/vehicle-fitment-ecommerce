package com.carmats.auth.service;

import com.carmats.auth.dto.request.LoginRequest;
import com.carmats.auth.dto.request.RefreshTokenRequest;
import com.carmats.auth.dto.request.RegisterRequest;
import com.carmats.auth.dto.response.AuthResponse;
import com.carmats.auth.entity.RefreshToken;
import com.carmats.auth.repository.RefreshTokenRepository;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.config.security.CustomUserDetails;
import com.carmats.config.security.JwtService;
import com.carmats.user.dto.response.UserResponse;
import com.carmats.user.entity.Role;
import com.carmats.user.entity.User;
import com.carmats.user.mapper.UserMapper;
import com.carmats.user.repository.RoleRepository;
import com.carmats.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException(
                    "EMAIL_ALREADY_EXISTS",
                    "Bu e-posta adresi zaten kullanılmaktadır."
            );
        }

        Role customerRole = roleRepository.findByName(Role.ROLE_CUSTOMER)
                .orElseThrow(() ->
                        new BusinessException(
                                "ROLE_NOT_FOUND",
                                "Müşteri rolü sistemde tanımlı değil."
                        )
                );

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.firstName().trim(),
                request.lastName().trim(),
                request.phoneNumber() != null ? request.phoneNumber().trim() : null
        );
        user.addRole(customerRole);

        User savedUser = userRepository.save(user);

        return createAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmailWithRoles(normalizedEmail)
                .orElseThrow(() ->
                        new BusinessException(
                                "INVALID_CREDENTIALS",
                                "E-posta adresi veya şifre hatalı."
                        )
                );

        if (!user.isActive()) {
            throw new BusinessException(
                    "ACCOUNT_DISABLED",
                    "Hesabınız devre dışı bırakılmıştır."
            );
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(
                    "INVALID_CREDENTIALS",
                    "E-posta adresi veya şifre hatalı."
            );
        }

        return createAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenWithUser(request.refreshToken())
                .orElseThrow(() ->
                        new BusinessException(
                                "INVALID_REFRESH_TOKEN",
                                "Geçersiz veya süresi dolmuş refresh token."
                        )
                );

        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            throw new BusinessException(
                    "INVALID_REFRESH_TOKEN",
                    "Geçersiz veya süresi dolmuş refresh token."
            );
        }

        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new BusinessException(
                    "ACCOUNT_DISABLED",
                    "Hesabınız devre dışı bırakılmıştır."
            );
        }

        refreshToken.revoke();

        return createAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "USER_NOT_FOUND",
                                "Kullanıcı bulunamadı."
                        )
                );

        return UserMapper.toUserResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        String accessToken = jwtService.generateAccessToken(userDetails);

        String rawRefreshToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusNanos(jwtService.getRefreshTokenExpirationMs() * 1_000_000);

        RefreshToken refreshToken = new RefreshToken(user, rawRefreshToken, expiryDate);
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(
                accessToken,
                rawRefreshToken,
                jwtService.getAccessTokenExpirationMs(),
                UserMapper.toUserResponse(user)
        );
    }
}
