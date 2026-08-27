package com.carmats.vehicle.controller;

import com.carmats.vehicle.dto.response.VehicleBrandResponse;
import com.carmats.vehicle.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(
            VehicleService vehicleService
    ) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/brands")
    public ResponseEntity<List<VehicleBrandResponse>> getBrands() {

        return ResponseEntity.ok(
                vehicleService.getBrands()
        );
    }
}