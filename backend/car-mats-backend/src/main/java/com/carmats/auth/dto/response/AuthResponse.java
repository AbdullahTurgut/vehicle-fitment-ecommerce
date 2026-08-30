package com.carmats.auth.dto.response;

import com.carmats.user.dto.response.UserResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInMs,
        UserResponse user
) {
    public static AuthResponse of(
            String accessToken,
            String refreshToken,
            long expiresInMs,
            UserResponse user
    ) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresInMs,
                user
        );
    }
}
