package com.carmats.vehicle.repository;

import com.carmats.vehicle.entity.VehicleVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleVariantRepository
        extends JpaRepository<VehicleVariant, UUID> {

    List<VehicleVariant>
    findAllByGenerationIdAndActiveTrueOrderBySortOrderAscNameAsc(
            UUID generationId
    );


}
