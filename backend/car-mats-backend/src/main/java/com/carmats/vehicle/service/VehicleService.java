package com.carmats.vehicle.service;

import com.carmats.common.exception.NotFoundException;
import com.carmats.vehicle.dto.response.VehicleBrandResponse;
import com.carmats.vehicle.mapper.VehicleMapper;
import com.carmats.vehicle.repository.VehicleBrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleBrandRepository brandRepository;

    public VehicleService(
            VehicleBrandRepository brandRepository
    ) {
        this.brandRepository = brandRepository;
    }

    public List<VehicleBrandResponse> getBrands() {

        return brandRepository
                .findAllByActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(VehicleMapper::toBrandResponse)
                .toList();
    }

    public void validateBrandExists(UUID brandId) {

        if (!brandRepository.existsById(brandId)) {

            throw new NotFoundException(
                    "VEHICLE_BRAND_NOT_FOUND",
                    "Araç markası bulunamadı."
            );
        }
    }
}