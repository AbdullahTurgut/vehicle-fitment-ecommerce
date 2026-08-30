package com.carmats.catalog.dto.response;

import java.util.UUID;

public record AdminProductCompatibilityResponse(
        UUID id,
        UUID vehicleVariantId,
        String variantName,
        String modelName,
        String brandName,
        Integer startYear,
        Integer endYear,
        String notes
) {
}
