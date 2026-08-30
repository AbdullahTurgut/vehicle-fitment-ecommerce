package com.carmats.catalog.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductDetailResponse(

        UUID id,
        String name,
        String slug,
        String sku,

        String shortDescription,
        String description,

        BigDecimal basePrice,
        BigDecimal salePrice,

        int stockQuantity,

        String manufacturerBrand,
        String material,

        CategoryResponse category,

        List<ProductImageResponse> images,
        List<ProductFeatureResponse> features

) {
}