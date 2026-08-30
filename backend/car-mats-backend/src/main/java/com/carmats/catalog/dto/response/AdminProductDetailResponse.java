package com.carmats.catalog.dto.response;

import com.carmats.catalog.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AdminProductDetailResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        String name,
        String slug,
        String sku,
        String shortDescription,
        String description,
        BigDecimal basePrice,
        BigDecimal salePrice,
        BigDecimal effectivePrice,
        int stockQuantity,
        ProductStatus status,
        boolean featured,
        String manufacturerBrand,
        String material,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ProductImageResponse> images,
        List<ProductFeatureResponse> features,
        List<AdminProductCompatibilityResponse> compatibilities
) {
}
