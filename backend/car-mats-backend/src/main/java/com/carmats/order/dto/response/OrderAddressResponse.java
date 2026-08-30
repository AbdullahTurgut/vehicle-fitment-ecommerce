package com.carmats.order.dto.response;

import com.carmats.order.entity.OrderAddressType;

import java.util.UUID;

public record OrderAddressResponse(
        UUID id,
        OrderAddressType addressType,
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
        String taxOffice
) {
}
