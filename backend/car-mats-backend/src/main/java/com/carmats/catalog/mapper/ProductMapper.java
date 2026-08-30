package com.carmats.catalog.mapper;

import com.carmats.catalog.dto.response.*;
import com.carmats.catalog.entity.*;
import com.carmats.catalog.repository.projection.ProductListProjection;

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

    public static ProductListResponse toListResponse(
            ProductListProjection projection
    ) {

        var effectivePrice =
                projection.getSalePrice() != null
                        ? projection.getSalePrice()
                        : projection.getBasePrice();

        boolean inStock =
                projection.getStockQuantity() != null
                        && projection.getStockQuantity() > 0;

        return new ProductListResponse(
                projection.getId(),
                projection.getName(),
                projection.getSlug(),
                projection.getSku(),
                projection.getBasePrice(),
                projection.getSalePrice(),
                effectivePrice,
                projection.getStockQuantity(),
                inStock,
                projection.getPrimaryImageUrl(),
                Boolean.TRUE.equals(projection.getFeatured())
        );
    }
}