package com.carmats.shipping.dto.response;

import com.carmats.shipping.entity.ShipmentStatus;
import com.carmats.shipping.entity.ShippingCarrier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        UUID orderId,
        String orderNumber,
        ShippingCarrier carrier,
        String trackingNumber,
        ShipmentStatus status,
        String recipientName,
        String recipientPhone,
        String deliveryAddressLine,
        String deliveryCity,
        String deliveryDistrict,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime estimatedDeliveryDate,
        String trackingUrl,
        List<ShipmentTrackingEventResponse> trackingEvents,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
