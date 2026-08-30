package com.carmats.shipping.mapper;

import com.carmats.shipping.dto.response.ShipmentResponse;
import com.carmats.shipping.dto.response.ShipmentTrackingEventResponse;
import com.carmats.shipping.entity.Shipment;
import com.carmats.shipping.entity.ShipmentTrackingEvent;

import java.util.Collections;
import java.util.List;

public final class ShipmentMapper {

    private ShipmentMapper() {
    }

    public static ShipmentResponse toResponse(Shipment shipment) {
        if (shipment == null) {
            return null;
        }

        List<ShipmentTrackingEventResponse> events = shipment.getTrackingEvents() != null
                ? shipment.getTrackingEvents().stream().map(ShipmentMapper::toTrackingEventResponse).toList()
                : Collections.emptyList();

        return new ShipmentResponse(
                shipment.getId(),
                shipment.getOrder() != null ? shipment.getOrder().getId() : null,
                shipment.getOrder() != null ? shipment.getOrder().getOrderNumber() : null,
                shipment.getCarrier(),
                shipment.getTrackingNumber(),
                shipment.getStatus(),
                shipment.getRecipientName(),
                shipment.getRecipientPhone(),
                shipment.getDeliveryAddressLine(),
                shipment.getDeliveryCity(),
                shipment.getDeliveryDistrict(),
                shipment.getShippedAt(),
                shipment.getDeliveredAt(),
                shipment.getEstimatedDeliveryDate(),
                shipment.getTrackingUrl(),
                events,
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }

    public static ShipmentTrackingEventResponse toTrackingEventResponse(ShipmentTrackingEvent event) {
        if (event == null) {
            return null;
        }

        return new ShipmentTrackingEventResponse(
                event.getId(),
                event.getStatus(),
                event.getLocation(),
                event.getDescription(),
                event.getEventTime()
        );
    }
}
