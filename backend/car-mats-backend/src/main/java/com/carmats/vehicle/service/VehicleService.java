package com.carmats.vehicle.service;

import com.carmats.common.exception.NotFoundException;
import com.carmats.vehicle.dto.response.VehicleBrandResponse;
import com.carmats.vehicle.dto.response.VehicleGenerationResponse;
import com.carmats.vehicle.dto.response.VehicleModelResponse;
import com.carmats.vehicle.dto.response.VehicleVariantResponse;
import com.carmats.vehicle.mapper.VehicleMapper;
import com.carmats.vehicle.repository.VehicleBrandRepository;
import com.carmats.vehicle.repository.VehicleGenerationRepository;
import com.carmats.vehicle.repository.VehicleModelRepository;
import com.carmats.vehicle.repository.VehicleVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleBrandRepository brandRepository;
    private final VehicleModelRepository modelRepository;
    private final VehicleGenerationRepository generationRepository;
    private final VehicleVariantRepository variantRepository;

    public VehicleService(
            VehicleBrandRepository brandRepository,
            VehicleModelRepository modelRepository,
            VehicleGenerationRepository generationRepository,
            VehicleVariantRepository variantRepository
    ) {
        this.brandRepository = brandRepository;
        this.modelRepository = modelRepository;
        this.generationRepository = generationRepository;
        this.variantRepository = variantRepository;
    }

    public List<VehicleBrandResponse> getBrands() {
        return brandRepository
                .findAllByActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(VehicleMapper::toBrandResponse)
                .toList();
    }

    public List<VehicleModelResponse> getModelsByBrandId(UUID brandId) {
        if (!brandRepository.existsById(brandId)) {
            throw new NotFoundException(
                    "VEHICLE_BRAND_NOT_FOUND",
                    "Araç markası bulunamadı."
            );
        }

        return modelRepository
                .findAllByBrandIdAndActiveTrueOrderBySortOrderAscNameAsc(brandId)
                .stream()
                .map(VehicleMapper::toModelResponse)
                .toList();
    }

    public List<VehicleGenerationResponse> getGenerationsByModelId(
            UUID modelId
    ) {
        if (!modelRepository.existsById(modelId)) {
            throw new NotFoundException(
                    "VEHICLE_MODEL_NOT_FOUND",
                    "Araç modeli bulunamadı."
            );
        }

        return generationRepository
                .findAllByModelIdAndActiveTrueOrderBySortOrderAscNameAsc(modelId)
                .stream()
                .map(VehicleMapper::toGenerationResponse)
                .toList();
    }

    public List<VehicleVariantResponse> getVariantsByGenerationId(
            UUID generationId
    ) {
        if (!generationRepository.existsById(generationId)) {
            throw new NotFoundException(
                    "VEHICLE_GENERATION_NOT_FOUND",
                    "Araç kasa veya nesil bilgisi bulunamadı."
            );
        }

        return variantRepository
                .findAllByGenerationIdAndActiveTrueOrderBySortOrderAscNameAsc(
                        generationId
                )
                .stream()
                .map(VehicleMapper::toVariantResponse)
                .toList();
    }
}