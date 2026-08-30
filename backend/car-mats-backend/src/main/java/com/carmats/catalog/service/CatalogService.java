package com.carmats.catalog.service;

import com.carmats.catalog.dto.response.*;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductImage;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.mapper.ProductMapper;
import com.carmats.catalog.repository.*;
import com.carmats.common.exception.NotFoundException;
import com.carmats.vehicle.repository.VehicleVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ProductFeatureRepository featureRepository;
    private final ProductCompatibilityRepository compatibilityRepository;
    private final VehicleVariantRepository vehicleVariantRepository;

    public CatalogService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ProductImageRepository imageRepository,
            ProductFeatureRepository featureRepository,
            ProductCompatibilityRepository compatibilityRepository,
            VehicleVariantRepository vehicleVariantRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.featureRepository = featureRepository;
        this.compatibilityRepository = compatibilityRepository;
        this.vehicleVariantRepository = vehicleVariantRepository;
    }

    public List<CategoryResponse> getCategories() {

        return categoryRepository
                .findAllByActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(ProductMapper::toCategoryResponse)
                .toList();
    }

    public List<ProductListResponse> getProducts() {

        return productRepository
                .findAllByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE)
                .stream()
                .map(this::toListResponse)
                .toList();
    }

    public ProductDetailResponse getProductBySlug(String slug) {

        Product product = productRepository
                .findBySlugAndStatus(slug, ProductStatus.ACTIVE)
                .orElseThrow(() ->
                        new NotFoundException(
                                "PRODUCT_NOT_FOUND",
                                "Ürün bulunamadı."
                        )
                );

        return toDetailResponse(product);
    }

    public List<ProductListResponse> getCompatibleProducts(
            UUID variantId,
            Integer year
    ) {

        if (!vehicleVariantRepository.existsByIdAndActiveTrue(variantId)) {

            throw new NotFoundException(
                    "VEHICLE_VARIANT_NOT_FOUND",
                    "Araç varyantı bulunamadı."
            );
        }

        return compatibilityRepository
                .findCompatibleProducts(variantId, year)
                .stream()
                .map(compatibility -> compatibility.getProduct())
                .map(this::toListResponse)
                .distinct()
                .toList();
    }

    private ProductListResponse toListResponse(Product product) {

        String primaryImageUrl = imageRepository
                .findAllByProductIdOrderBySortOrderAsc(product.getId())
                .stream()
                .filter(ProductImage::isPrimary)
                .map(ProductImage::getUrl)
                .findFirst()
                .orElse(null);

        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getBasePrice(),
                product.getSalePrice(),
                product.getStockQuantity(),
                primaryImageUrl,
                product.isFeatured()
        );
    }

    private ProductDetailResponse toDetailResponse(Product product) {

        var images = imageRepository
                .findAllByProductIdOrderBySortOrderAsc(product.getId())
                .stream()
                .map(ProductMapper::toImageResponse)
                .toList();

        var features = featureRepository
                .findAllByProductIdOrderBySortOrderAsc(product.getId())
                .stream()
                .map(ProductMapper::toFeatureResponse)
                .toList();

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getShortDescription(),
                product.getDescription(),
                product.getBasePrice(),
                product.getSalePrice(),
                product.getStockQuantity(),
                product.getManufacturerBrand(),
                product.getMaterial(),
                ProductMapper.toCategoryResponse(product.getCategory()),
                images,
                features
        );
    }
}