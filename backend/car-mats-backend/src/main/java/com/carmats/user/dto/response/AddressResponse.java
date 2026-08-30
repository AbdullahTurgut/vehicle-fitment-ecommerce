package com.carmats.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        UUID userId,
        String title,
        String firstName,
        String lastName,
        String phoneNumber,
        String city,
        String district,
        String neighborhood,
        String addressLine,
        String postalCode,
        String companyName,
        String taxNumber,
        String taxOffice,
        boolean defaultDelivery,
        boolean defaultBilling,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
