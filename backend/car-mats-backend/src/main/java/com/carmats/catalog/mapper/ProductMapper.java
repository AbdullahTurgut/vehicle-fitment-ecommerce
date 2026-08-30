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

    public static AdminCategoryResponse toAdminCategoryResponse(
            Category category
    ) {
        return new AdminCategoryResponse(
                category.getId(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getImageUrl(),
                category.isActive(),
                category.getSortOrder(),
                category.getCreatedAt(),
                category.getUpdatedAt()
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

    public static AdminProductListResponse toAdminProductListResponse(
            Product product,
            String primaryImageUrl
    ) {
        return new AdminProductListResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getBasePrice(),
                product.getSalePrice(),
                product.getEffectivePrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.isFeatured(),
                primaryImageUrl,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public static AdminProductDetailResponse toAdminProductDetailResponse(
            Product product,
            List<ProductImage> images,
            List<ProductFeature> features,
            List<ProductCompatibility> compatibilities
    ) {
        return new AdminProductDetailResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getShortDescription(),
                product.getDescription(),
                product.getBasePrice(),
                product.getSalePrice(),
                product.getEffectivePrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.isFeatured(),
                product.getManufacturerBrand(),
                product.getMaterial(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                images.stream().map(ProductMapper::toImageResponse).toList(),
                features.stream().map(ProductMapper::toFeatureResponse).toList(),
                compatibilities.stream().map(ProductMapper::toAdminCompatibilityResponse).toList()
        );
    }

    public static AdminProductCompatibilityResponse toAdminCompatibilityResponse(
            ProductCompatibility compatibility
    ) {
        var variant = compatibility.getVehicleVariant();
        var generation = variant.getGeneration();
        var model = generation.getModel();
        var brand = model.getBrand();

        return new AdminProductCompatibilityResponse(
                compatibility.getId(),
                variant.getId(),
                variant.getName(),
                model.getName(),
                brand.getName(),
                compatibility.getStartYear(),
                compatibility.getEndYear(),
                compatibility.getNotes()
        );
    }
}