package com.carmats.catalog.dto.response;

import com.carmats.catalog.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminProductListResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        String name,
        String slug,
        String sku,
        BigDecimal basePrice,
        BigDecimal salePrice,
        BigDecimal effectivePrice,
        int stockQuantity,
        ProductStatus status,
        boolean featured,
        String primaryImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
