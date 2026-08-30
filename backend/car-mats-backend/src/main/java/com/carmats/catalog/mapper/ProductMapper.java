package com.carmats.catalog.mapper;

import com.carmats.catalog.dto.response.*;
import com.carmats.catalog.entity.*;

import java.util.List;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static CategoryResponse toCategoryResponse(
            Category category
    ) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getImageUrl()
        );
    }

    public static ProductImageResponse toImageResponse(
            ProductImage image
    ) {
        return new ProductImageResponse(
                image.getId(),
                image.getUrl(),
                image.getAltText(),
                image.isPrimary()
        );
    }

    public static ProductFeatureResponse toFeatureResponse(
            ProductFeature feature
    ) {
        return new ProductFeatureResponse(
                feature.getId(),
                feature.getTitle(),
                feature.getDescription(),
                feature.getIcon()
        );
    }
}