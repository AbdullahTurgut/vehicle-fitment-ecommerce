package com.carmats.shipping.controller;

import com.carmats.config.security.CustomUserDetails;
import com.carmats.shipping.dto.response.ShipmentResponse;
import com.carmats.shipping.service.ShippingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping("/orders/{orderNumber}")
    public ResponseEntity<ShipmentResponse> getShipmentByOrderNumber(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String orderNumber
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        ShipmentResponse response = shippingService.getShipmentByOrderNumber(userId, orderNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getShipmentByTrackingNumber(
            @PathVariable String trackingNumber
    ) {
        ShipmentResponse response = shippingService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(response);
    }
}
