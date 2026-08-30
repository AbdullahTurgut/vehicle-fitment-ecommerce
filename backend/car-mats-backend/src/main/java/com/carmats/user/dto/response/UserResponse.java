package com.carmats.user.dto.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        boolean active,
        Set<String> roles,
        LocalDateTime createdAt
) {
}
