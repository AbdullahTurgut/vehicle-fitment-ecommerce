package com.carmats.catalog.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductListResponse(

        UUID id,

        String name,
        String slug,
        String sku,

        BigDecimal basePrice,
        BigDecimal salePrice,
        BigDecimal effectivePrice,

        int stockQuantity,
        boolean inStock,

        String primaryImageUrl,

        boolean featured

) {
}