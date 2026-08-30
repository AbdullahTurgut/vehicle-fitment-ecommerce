package com.carmats.shipping.dto.response;

import com.carmats.shipping.entity.ShipmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentTrackingEventResponse(
        UUID id,
        ShipmentStatus status,
        String location,
        String description,
        LocalDateTime eventTime
) {
}
