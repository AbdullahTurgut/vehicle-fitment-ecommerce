package com.carmats.auth.controller;

import com.carmats.auth.dto.request.LoginRequest;
import com.carmats.auth.dto.request.RefreshTokenRequest;
import com.carmats.auth.dto.request.RegisterRequest;
import com.carmats.auth.dto.response.AuthResponse;
import com.carmats.auth.service.AuthService;
import com.carmats.config.security.CustomUserDetails;
import com.carmats.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Kimlik Doğrulama", description = "Kayıt, giriş, token yenileme ve profil işlemleri")
@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Yeni müşteri kaydı", description = "Platforma yeni müşteri hesabı açar ve JWT token döner.")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Kullanıcı girişi", description = "E-posta ve şifre ile giriş yapar, access ve refresh token döner.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Token yenileme", description = "Geçerli bir refresh token ile yeni access ve refresh token üretir.")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(summary = "Mevcut kullanıcı bilgisi", description = "Giriş yapmış kullanıcının profil ve rol bilgilerini döner.")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getId()));
    }
}
