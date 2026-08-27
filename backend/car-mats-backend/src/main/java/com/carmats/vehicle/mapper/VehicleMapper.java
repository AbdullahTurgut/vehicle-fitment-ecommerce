package com.carmats.vehicle.mapper;

import com.carmats.vehicle.dto.response.VehicleBrandResponse;
import com.carmats.vehicle.entity.VehicleBrand;

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
}