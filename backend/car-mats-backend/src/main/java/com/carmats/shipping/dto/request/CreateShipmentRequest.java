package com.carmats.shipping.dto.request;

import com.carmats.shipping.entity.ShippingCarrier;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateShipmentRequest(
        @NotNull(message = "Sipariş ID zorunludur.")
        UUID orderId,

        @NotNull(message = "Kargo firması zorunludur.")
        ShippingCarrier carrier,

        String trackingNumber,

        LocalDateTime estimatedDeliveryDate
) {
}
