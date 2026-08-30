package com.carmats.vehicle.repository;

import com.carmats.vehicle.entity.VehicleBrand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleBrandRepository
        extends JpaRepository<VehicleBrand, UUID> {

    List<VehicleBrand> findAllByActiveTrueOrderBySortOrderAscNameAsc();

    boolean existsByIdAndActiveTrue(UUID id);
}