package com.carmats.vehicle.dto.response;

import java.util.UUID;

public record VehicleGenerationResponse(
        UUID id,
        String name,
        String code,
        Integer startYear,
        Integer endYear
) {
}
