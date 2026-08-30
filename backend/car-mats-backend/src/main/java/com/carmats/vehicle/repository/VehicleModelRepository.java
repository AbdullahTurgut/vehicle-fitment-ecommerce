package com.carmats.vehicle.repository;

import com.carmats.vehicle.entity.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleModelRepository
        extends JpaRepository<VehicleModel, UUID> {

    List<VehicleModel>
    findAllByBrandIdAndActiveTrueOrderBySortOrderAscNameAsc(UUID brandId);

    boolean existsByIdAndActiveTrue(UUID id);
}