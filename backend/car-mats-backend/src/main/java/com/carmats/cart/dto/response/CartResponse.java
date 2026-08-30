package com.carmats.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        UUID userId,
        String guestToken,
        List<CartItemResponse> items,
        int totalQuantity,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal totalAmount
) {
    public static CartResponse empty(UUID id, UUID userId, String guestToken) {
        return new CartResponse(
                id,
                userId,
                guestToken,
                List.of(),
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}
