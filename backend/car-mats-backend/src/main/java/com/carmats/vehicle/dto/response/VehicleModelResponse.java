package com.carmats.vehicle.dto.response;

import java.util.UUID;

public record VehicleModelResponse(
        UUID id,
        String name,
        String slug
) {
}
