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

        int stockQuantity,

        String primaryImageUrl,

        boolean featured

) {
}