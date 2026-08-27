package com.carmats.vehicle.dto.response;

import java.util.UUID;

public record VehicleBrandResponse(
        UUID id,
        String name,
        String slug,
        String logoUrl
) {
}