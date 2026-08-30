package com.carmats.order.dto.response;

import com.carmats.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID id,
        String orderNumber,
        OrderStatus status,
        int totalItems,
        BigDecimal grandTotal,
        String currency,
        LocalDateTime createdAt
) {
}
