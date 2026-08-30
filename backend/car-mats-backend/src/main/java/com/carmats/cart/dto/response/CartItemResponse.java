package com.carmats.cart.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        String productName,
        String productSlug,
        String productSku,
        String primaryImageUrl,
        UUID vehicleVariantId,
        String vehicleVariantName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        int stockQuantity,
        boolean available
) {
}
