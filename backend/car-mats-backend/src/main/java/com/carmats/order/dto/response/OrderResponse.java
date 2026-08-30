package com.carmats.order.dto.response;

import com.carmats.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        UUID userId,
        String guestEmail,
        String guestFirstName,
        String guestLastName,
        String guestPhoneNumber,
        OrderStatus status,
        String currency,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountTotal,
        BigDecimal grandTotal,
        String customerNotes,
        String adminNotes,
        List<OrderItemResponse> items,
        OrderAddressResponse deliveryAddress,
        OrderAddressResponse billingAddress,
        List<OrderStatusHistoryResponse> statusHistory,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
