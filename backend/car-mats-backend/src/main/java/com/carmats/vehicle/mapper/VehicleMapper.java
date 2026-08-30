package com.carmats.vehicle.mapper;

import com.carmats.vehicle.dto.response.VehicleBrandResponse;
import com.carmats.vehicle.dto.response.VehicleGenerationResponse;
import com.carmats.vehicle.dto.response.VehicleModelResponse;
import com.carmats.vehicle.dto.response.VehicleVariantResponse;
import com.carmats.vehicle.entity.VehicleBrand;
import com.carmats.vehicle.entity.VehicleGeneration;
import com.carmats.vehicle.entity.VehicleModel;
import com.carmats.vehicle.entity.VehicleVariant;

public final class VehicleMapper {

    private VehicleMapper() {
    }

    public static VehicleBrandResponse toBrandResponse(
            VehicleBrand brand
    ) {
        return new VehicleBrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getSlug(),
                brand.getLogoUrl()
        );
    }

    public static VehicleModelResponse toModelResponse(
            VehicleModel model
    ) {
        return new VehicleModelResponse(
                model.getId(),
                model.getName(),
                model.getSlug()
        );
    }

    public static VehicleGenerationResponse toGenerationResponse(
            VehicleGeneration generation
    ) {
        return new VehicleGenerationResponse(
                generation.getId(),
                generation.getName(),
                generation.getCode(),
                generation.getStartYear(),
                generation.getEndYear()
        );
    }

    public static VehicleVariantResponse toVariantResponse(
            VehicleVariant variant
    ) {
        return new VehicleVariantResponse(
                variant.getId(),
                variant.getName(),
                variant.getBodyType(),
                variant.getFuelType(),
                variant.getSeatCount(),
                variant.getTrunkType()
        );
    }
}