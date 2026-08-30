package com.carmats.shipping.controller;

import com.carmats.config.security.CustomUserDetails;
import com.carmats.shipping.dto.request.CreateShipmentRequest;
import com.carmats.shipping.dto.request.UpdateShipmentStatusRequest;
import com.carmats.shipping.dto.response.ShipmentResponse;
import com.carmats.shipping.service.ShippingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/shipments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShippingController {

    private final ShippingService shippingService;

    public AdminShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateShipmentRequest request
    ) {
        String adminEmail = userDetails != null ? userDetails.getUsername() : "ADMIN";
        ShipmentResponse response = shippingService.createShipment(request, adminEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponse> getShipmentById(
            @PathVariable UUID shipmentId
    ) {
        ShipmentResponse response = shippingService.getShipmentById(shipmentId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{shipmentId}/status")
    public ResponseEntity<ShipmentResponse> updateShipmentStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID shipmentId,
            @Valid @RequestBody UpdateShipmentStatusRequest request
    ) {
        String adminEmail = userDetails != null ? userDetails.getUsername() : "ADMIN";
        ShipmentResponse response = shippingService.updateShipmentStatus(shipmentId, request, adminEmail);
        return ResponseEntity.ok(response);
    }
}
