package com.carmats.order.dto.request;

import com.carmats.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Sipariş durumu zorunludur.")
        OrderStatus status,

        String note
) {
}
