package com.carmats.catalog.dto.response;

import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String url,
        String altText,
        boolean primary
) {
}
