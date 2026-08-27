package com.carmats.vehicle.dto.response;

import java.util.UUID;

public record VehicleVariantResponse(
        UUID id,
        String name,
        String bodyType,
        String fuelType,
        Integer seatCount,
        String trunkType
) {
}