package com.carmats.vehicle.controller;

import com.carmats.vehicle.dto.response.VehicleBrandResponse;
import com.carmats.vehicle.dto.response.VehicleGenerationResponse;
import com.carmats.vehicle.dto.response.VehicleModelResponse;
import com.carmats.vehicle.dto.response.VehicleVariantResponse;
import com.carmats.vehicle.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/brands")
    public ResponseEntity<List<VehicleBrandResponse>> getBrands() {
        return ResponseEntity.ok(
                vehicleService.getBrands()
        );
    }

    @GetMapping("/brands/{brandId}/models")
    public ResponseEntity<List<VehicleModelResponse>> getModels(
            @PathVariable UUID brandId
    ) {
        return ResponseEntity.ok(
                vehicleService.getModelsByBrandId(brandId)
        );
    }

    @GetMapping("/models/{modelId}/generations")
    public ResponseEntity<List<VehicleGenerationResponse>> getGenerations(
            @PathVariable UUID modelId
    ) {
        return ResponseEntity.ok(
                vehicleService.getGenerationsByModelId(modelId)
        );
    }

    @GetMapping("/generations/{generationId}/variants")
    public ResponseEntity<List<VehicleVariantResponse>> getVariants(
            @PathVariable UUID generationId
    ) {
        return ResponseEntity.ok(
                vehicleService.getVariantsByGenerationId(generationId)
        );
    }
}