package com.carmats.catalog.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminCategoryResponse(
        UUID id,
        UUID parentId,
        String name,
        String slug,
        String description,
        String imageUrl,
        boolean active,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
