package com.carmats.favorite.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FavoriteResponse(
        UUID id,
        UUID productId,
        String productName,
        String productSlug,
        String productSku,
        BigDecimal basePrice,
        BigDecimal salePrice,
        String categoryName,
        LocalDateTime createdAt
) {
}
