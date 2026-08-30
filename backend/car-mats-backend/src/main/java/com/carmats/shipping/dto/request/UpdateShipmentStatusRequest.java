package com.carmats.shipping.dto.request;

import com.carmats.shipping.entity.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateShipmentStatusRequest(
        @NotNull(message = "Kargo durumu zorunludur.")
        ShipmentStatus status,

        String location,

        @NotBlank(message = "Açıklama zorunludur.")
        String description
) {
}
