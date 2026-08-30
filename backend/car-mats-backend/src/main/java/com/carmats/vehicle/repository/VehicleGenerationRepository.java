package com.carmats.vehicle.repository;

import com.carmats.vehicle.entity.VehicleGeneration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleGenerationRepository
        extends JpaRepository<VehicleGeneration, UUID> {

    List<VehicleGeneration>
    findAllByModelIdAndActiveTrueOrderBySortOrderAscNameAsc(UUID modelId);

    boolean existsByIdAndActiveTrue(UUID id);
}