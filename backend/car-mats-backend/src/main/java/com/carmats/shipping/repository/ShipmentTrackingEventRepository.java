package com.carmats.shipping.repository;

import com.carmats.shipping.entity.ShipmentTrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShipmentTrackingEventRepository extends JpaRepository<ShipmentTrackingEvent, UUID> {

    List<ShipmentTrackingEvent> findAllByShipmentIdOrderByEventTimeDesc(UUID shipmentId);
}
