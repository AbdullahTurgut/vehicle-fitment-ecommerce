package com.carmats.catalog.service;

import com.carmats.catalog.dto.response.CategoryResponse;
import com.carmats.catalog.dto.response.ProductDetailResponse;
import com.carmats.catalog.dto.response.ProductListResponse;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductImage;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.mapper.ProductMapper;
import com.carmats.catalog.repository.CategoryRepository;
import com.carmats.catalog.repository.ProductCompatibilityRepository;
import com.carmats.catalog.repository.ProductFeatureRepository;
import com.carmats.catalog.repository.ProductImageRepository;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.common.response.PageResponse;
import com.carmats.vehicle.entity.VehicleGeneration;
import com.carmats.vehicle.entity.VehicleVariant;
import com.carmats.vehicle.repository.VehicleVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    /*
     * Aktif kategorileri listeler.
     */
    public List<CategoryResponse> getCategories() {

        return categoryRepository
                .findAllByActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(ProductMapper::toCategoryResponse)
                .toList();
    }

    /*
     * Aktif ürünleri sayfalı olarak listeler.
     * categorySlug null ise tüm aktif ürünleri getirir.
     */
    public PageResponse<ProductListResponse> getProducts(
            String categorySlug,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size
        );

        Page<ProductListResponse> products =
                productRepository
                        .findPublicProducts(
                                normalizeCategorySlug(categorySlug),
                                pageable
                        )
                        .map(ProductMapper::toListResponse);

        return PageResponse.from(products);
    }

    /*
     * Slug değerine göre public ürün detayını getirir.
     *
     * Ürün ACTIVE olmalı ve kategorisi aktif olmalı.
     */
    public ProductDetailResponse getProductBySlug(String slug) {

        Product product = productRepository
                .findPublicProductBySlug(
                        slug,
                        ProductStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new NotFoundException(
                                "PRODUCT_NOT_FOUND",
                                "Ürün bulunamadı."
                        )
                );

        return toDetailResponse(product);
    }

    /*
     * Araç varyantı ve model yılına göre
     * uyumlu ürünleri getirir.
     */
    public List<ProductListResponse> getCompatibleProducts(
            UUID variantId,
            Integer year
    ) {

        VehicleVariant variant = vehicleVariantRepository
                .findByIdAndActiveTrue(variantId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "VEHICLE_VARIANT_NOT_FOUND",
                                "Araç varyantı bulunamadı."
                        )
                );

        validateVehicleYear(
                variant,
                year
        );

        return compatibilityRepository
                .findCompatibleProducts(
                        variantId,
                        year
                )
                .stream()
                .map(compatibility -> compatibility.getProduct())
                .distinct()
                .map(this::toListResponse)
                .toList();
    }

    /*
     * Compatible products tarafında Product entity geldiği için
     * liste response'una dönüştürür.
     *
     * Not:
     * Public products endpoint'inde projection kullanıldığı için
     * N+1 problemi orada çözülmüş durumda.
     */
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
                product.getEffectivePrice(),
                product.getStockQuantity(),
                product.isInStock(),
                primaryImageUrl,
                product.isFeatured()
        );
    }

    /*
     * Product detail response hazırlanır.
     */
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
                product.getEffectivePrice(),

                product.getStockQuantity(),
                product.isInStock(),

                product.getManufacturerBrand(),
                product.getMaterial(),

                ProductMapper.toCategoryResponse(
                        product.getCategory()
                ),

                images,
                features
        );
    }

    /*
     * Kullanıcı boş category parametresi gönderirse
     * bunu null olarak değerlendiriyoruz.
     *
     * Örnek:
     * ?category=
     */
    private String normalizeCategorySlug(String categorySlug) {

        if (categorySlug == null) {
            return null;
        }

        String normalized = categorySlug.trim();

        if (normalized.isEmpty()) {
            return null;
        }

        return normalized;
    }

    /*
     * Seçilen model yılının ilgili kasa/nesil
     * aralığında olup olmadığını kontrol eder.
     */
    private void validateVehicleYear(
            VehicleVariant variant,
            Integer year
    ) {

        if (year == null) {
            return;
        }

        VehicleGeneration generation =
                variant.getGeneration();

        Integer startYear =
                generation.getStartYear();

        Integer endYear =
                generation.getEndYear();

        if (startYear != null && year < startYear) {

            throw new BusinessException(
                    "INVALID_VEHICLE_YEAR",
                    "Seçilen yıl bu araç kasası için geçerli değildir."
            );
        }

        if (endYear != null && year > endYear) {

            throw new BusinessException(
                    "INVALID_VEHICLE_YEAR",
                    "Seçilen yıl bu araç kasası için geçerli değildir."
            );
        }
    }
}