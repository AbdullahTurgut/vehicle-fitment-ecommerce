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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Kimlik Doğrulama", description = "Kayıt, giriş, token yenileme ve profil işlemleri")
@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "carmats_refresh_token";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Yeni müşteri kaydı", description = "Platforma yeni müşteri hesabı açar ve JWT token döner.")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        ResponseCookie cookie = createRefreshTokenCookie(response.refreshToken(), 7 * 24 * 3600);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @Operation(summary = "Kullanıcı girişi", description = "E-posta ve şifre ile giriş yapar, access ve refresh token döner.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        ResponseCookie cookie = createRefreshTokenCookie(response.refreshToken(), 7 * 24 * 3600);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @Operation(summary = "Token yenileme", description = "Geçerli bir refresh token ile yeni access ve refresh token üretir.")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieRefreshToken
    ) {
        String token = (request != null && request.refreshToken() != null && !request.refreshToken().isBlank())
                ? request.refreshToken()
                : cookieRefreshToken;

        AuthResponse response = authService.refreshToken(token);
        ResponseCookie cookie = createRefreshTokenCookie(response.refreshToken(), 7 * 24 * 3600);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @Operation(summary = "Oturum kapatma (Logout)", description = "Refresh token'ı veritabanında geçersiz kılar ve HttpOnly çerezi temizler.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieRefreshToken
    ) {
        String token = (request != null && request.refreshToken() != null && !request.refreshToken().isBlank())
                ? request.refreshToken()
                : cookieRefreshToken;

        if (token != null && !token.isBlank()) {
            authService.logout(token);
        }

        ResponseCookie deleteCookie = createRefreshTokenCookie("", 0);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @Operation(summary = "Mevcut kullanıcı bilgisi", description = "Giriş yapmış kullanıcının profil ve rol bilgilerini döner.")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getId()));
    }

    private ResponseCookie createRefreshTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false) // Dynamic or set via proxy
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
