package com.carmats.order.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
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
        BigDecimal lineTotal
) {
}
