package com.carmats.catalog.dto.response;

import java.util.UUID;

public record ProductFeatureResponse(
        UUID id,
        String title,
        String description,
        String icon
) {
}