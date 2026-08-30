package com.carmats.checkout.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutItemDto(
        UUID productId,
        String productName,
        String productSlug,
        String productSku,
        String primaryImageUrl,
        UUID vehicleVariantId,
        String vehicleVariantName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
