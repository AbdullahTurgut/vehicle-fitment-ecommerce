package com.carmats.user.controller;

import com.carmats.config.security.CustomUserDetails;
import com.carmats.user.dto.request.ChangePasswordRequest;
import com.carmats.user.dto.request.UpdateProfileRequest;
import com.carmats.user.dto.response.UserResponse;
import com.carmats.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Kullanıcı Profili", description = "Profil görüntüleme, güncelleme ve şifre değiştirme")
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Kullanıcı profili", description = "Oturum açmış kullanıcının profil bilgilerini getirir.")
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getId()));
    }

    @Operation(summary = "Profil güncelleme", description = "Kullanıcının ad, soyad ve telefon bilgilerini günceller.")
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getId(), request));
    }

    @Operation(summary = "Şifre değiştirme", description = "Mevcut şifreyi doğrulayarak yeni şifre belirler.")
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
