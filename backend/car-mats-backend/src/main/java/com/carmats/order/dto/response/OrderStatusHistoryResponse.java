package com.carmats.order.dto.response;

import com.carmats.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusHistoryResponse(
        UUID id,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        String note,
        String changedBy,
        LocalDateTime createdAt
) {
}
