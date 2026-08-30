package com.carmats.catalog.service;

import com.carmats.catalog.dto.request.*;
import com.carmats.catalog.dto.response.*;
import com.carmats.catalog.entity.*;
import com.carmats.catalog.mapper.ProductMapper;
import com.carmats.catalog.repository.*;
import com.carmats.catalog.util.SlugUtils;
import com.carmats.common.response.PageResponse;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.vehicle.entity.VehicleVariant;
import com.carmats.vehicle.repository.VehicleVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository imageRepository;
    private final ProductFeatureRepository featureRepository;
    private final ProductCompatibilityRepository compatibilityRepository;
    private final VehicleVariantRepository vehicleVariantRepository;

    public AdminProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductImageRepository imageRepository,
            ProductFeatureRepository featureRepository,
            ProductCompatibilityRepository compatibilityRepository,
            VehicleVariantRepository vehicleVariantRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.featureRepository = featureRepository;
        this.compatibilityRepository = compatibilityRepository;
        this.vehicleVariantRepository = vehicleVariantRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminProductListResponse> getProducts(
            String search,
            UUID categoryId,
            ProductStatus status,
            Pageable pageable
    ) {
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;

        Page<Product> page = productRepository.findAdminProducts(
                searchParam,
                categoryId,
                status,
                pageable
        );

        List<AdminProductListResponse> content = page.getContent().stream()
                .map(product -> {
                    String primaryImageUrl = imageRepository
                            .findAllByProductIdOrderBySortOrderAsc(product.getId())
                            .stream()
                            .filter(ProductImage::isPrimary)
                            .map(ProductImage::getUrl)
                            .findFirst()
                            .orElse(null);

                    return ProductMapper.toAdminProductListResponse(product, primaryImageUrl);
                })
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public AdminProductDetailResponse getProductById(UUID id) {
        Product product = findProductById(id);
        List<ProductImage> images = imageRepository.findAllByProductIdOrderBySortOrderAsc(id);
        List<ProductFeature> features = featureRepository.findAllByProductIdOrderBySortOrderAsc(id);
        List<ProductCompatibility> compatibilities = compatibilityRepository.findAllByProductId(id);

        return ProductMapper.toAdminProductDetailResponse(product, images, features, compatibilities);
    }

    public AdminProductDetailResponse createProduct(CreateProductRequest request) {
        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "CATEGORY_NOT_FOUND",
                                "Kategori bulunamadı."
                        )
                );

        String slug = resolveSlug(request.slug(), request.name());

        if (productRepository.existsBySlug(slug)) {
            throw new BusinessException(
                    "PRODUCT_SLUG_ALREADY_EXISTS",
                    "Bu ürün slug değeri zaten kullanılıyor."
            );
        }

        String sku = request.sku().trim();
        if (productRepository.existsBySku(sku)) {
            throw new BusinessException(
                    "PRODUCT_SKU_ALREADY_EXISTS",
                    "Bu SKU değeri zaten kullanılıyor."
            );
        }

        validatePriceRange(request.basePrice(), request.salePrice());

        ProductStatus status = request.status() != null ? request.status() : ProductStatus.DRAFT;
        boolean featured = Boolean.TRUE.equals(request.featured());
        int stockQuantity = request.stockQuantity() != null ? request.stockQuantity() : 0;

        Product product = new Product(
                category,
                request.name().trim(),
                slug,
                sku,
                request.shortDescription(),
                request.description(),
                request.basePrice(),
                request.salePrice(),
                stockQuantity,
                status,
                featured,
                request.manufacturerBrand(),
                request.material()
        );

        Product saved = productRepository.save(product);
        return ProductMapper.toAdminProductDetailResponse(saved, List.of(), List.of(), List.of());
    }

    public AdminProductDetailResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = findProductById(id);

        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "CATEGORY_NOT_FOUND",
                                "Kategori bulunamadı."
                        )
                );

        String slug = resolveSlug(request.slug(), request.name());

        if (productRepository.existsBySlugAndIdNot(slug, id)) {
            throw new BusinessException(
                    "PRODUCT_SLUG_ALREADY_EXISTS",
                    "Bu ürün slug değeri zaten kullanılıyor."
            );
        }

        String sku = request.sku().trim();
        if (productRepository.existsBySkuAndIdNot(sku, id)) {
            throw new BusinessException(
                    "PRODUCT_SKU_ALREADY_EXISTS",
                    "Bu SKU değeri zaten kullanılıyor."
            );
        }

        validatePriceRange(request.basePrice(), request.salePrice());

        boolean featured = Boolean.TRUE.equals(request.featured());
        int stockQuantity = request.stockQuantity() != null ? request.stockQuantity() : product.getStockQuantity();

        product.update(
                category,
                request.name().trim(),
                slug,
                sku,
                request.shortDescription(),
                request.description(),
                request.basePrice(),
                request.salePrice(),
                stockQuantity,
                featured,
                request.manufacturerBrand(),
                request.material()
        );

        List<ProductImage> images = imageRepository.findAllByProductIdOrderBySortOrderAsc(id);
        List<ProductFeature> features = featureRepository.findAllByProductIdOrderBySortOrderAsc(id);
        List<ProductCompatibility> compatibilities = compatibilityRepository.findAllByProductId(id);

        return ProductMapper.toAdminProductDetailResponse(product, images, features, compatibilities);
    }

    public AdminProductDetailResponse updateProductStatus(UUID id, UpdateProductStatusRequest request) {
        Product product = findProductById(id);
        product.setStatus(request.status());

        List<ProductImage> images = imageRepository.findAllByProductIdOrderBySortOrderAsc(id);
        List<ProductFeature> features = featureRepository.findAllByProductIdOrderBySortOrderAsc(id);
        List<ProductCompatibility> compatibilities = compatibilityRepository.findAllByProductId(id);

        return ProductMapper.toAdminProductDetailResponse(product, images, features, compatibilities);
    }

    public ProductImageResponse addProductImage(UUID productId, CreateProductImageRequest request) {
        Product product = findProductById(productId);

        boolean isPrimary = Boolean.TRUE.equals(request.primary());

        if (isPrimary) {
            imageRepository.findAllByProductIdOrderBySortOrderAsc(productId)
                    .forEach(existingImage -> {
                        if (existingImage.isPrimary()) {
                            existingImage.setPrimary(false);
                        }
                    });
        }

        int sortOrder = request.sortOrder() != null ? request.sortOrder() : 0;

        ProductImage image = new ProductImage(
                product,
                request.url().trim(),
                request.altText(),
                sortOrder,
                isPrimary
        );

        ProductImage saved = imageRepository.save(image);
        return ProductMapper.toImageResponse(saved);
    }

    public void deleteProductImage(UUID productId, UUID imageId) {
        // Ensure product exists
        findProductById(productId);

        ProductImage image = imageRepository
                .findByIdAndProductId(imageId, productId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "PRODUCT_IMAGE_NOT_FOUND",
                                "Ürün görseli bulunamadı."
                        )
                );

        imageRepository.delete(image);
    }

    public ProductFeatureResponse addProductFeature(UUID productId, CreateProductFeatureRequest request) {
        Product product = findProductById(productId);

        int sortOrder = request.sortOrder() != null ? request.sortOrder() : 0;

        ProductFeature feature = new ProductFeature(
                product,
                request.title().trim(),
                request.description(),
                request.icon(),
                sortOrder
        );

        ProductFeature saved = featureRepository.save(feature);
        return ProductMapper.toFeatureResponse(saved);
    }

    public void deleteProductFeature(UUID productId, UUID featureId) {
        // Ensure product exists
        findProductById(productId);

        ProductFeature feature = featureRepository
                .findByIdAndProductId(featureId, productId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "PRODUCT_FEATURE_NOT_FOUND",
                                "Ürün özelliği bulunamadı."
                        )
                );

        featureRepository.delete(feature);
    }

    public AdminProductCompatibilityResponse addProductCompatibility(
            UUID productId,
            CreateProductCompatibilityRequest request
    ) {
        Product product = findProductById(productId);

        VehicleVariant variant = vehicleVariantRepository
                .findById(request.vehicleVariantId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "VEHICLE_VARIANT_NOT_FOUND",
                                "Araç varyantı bulunamadı."
                        )
                );

        if (request.startYear() != null && request.endYear() != null
                && request.startYear() > request.endYear()) {
            throw new BusinessException(
                    "INVALID_YEAR_RANGE",
                    "Başlangıç yılı bitiş yılından büyük olamaz."
            );
        }

        var generation = variant.getGeneration();
        if (request.startYear() != null) {
            if ((generation.getStartYear() != null && request.startYear() < generation.getStartYear())
                    || (generation.getEndYear() != null && request.startYear() > generation.getEndYear())) {
                throw new BusinessException(
                        "INVALID_VEHICLE_YEAR",
                        "Uyumluluk yılı kasa üretim yılı aralığında olmalıdır."
                );
            }
        }

        if (request.endYear() != null) {
            if ((generation.getStartYear() != null && request.endYear() < generation.getStartYear())
                    || (generation.getEndYear() != null && request.endYear() > generation.getEndYear())) {
                throw new BusinessException(
                        "INVALID_VEHICLE_YEAR",
                        "Uyumluluk yılı kasa üretim yılı aralığında olmalıdır."
                );
            }
        }

        if (compatibilityRepository.existsByProductIdAndVehicleVariantIdAndStartYearAndEndYear(
                productId,
                request.vehicleVariantId(),
                request.startYear(),
                request.endYear()
        )) {
            throw new BusinessException(
                    "PRODUCT_COMPATIBILITY_ALREADY_EXISTS",
                    "Bu araç varyantı ve yıl aralığı için uyumluluk kaydı zaten mevcut."
            );
        }

        ProductCompatibility compatibility = new ProductCompatibility(
                product,
                variant,
                request.startYear(),
                request.endYear(),
                request.notes()
        );

        ProductCompatibility saved = compatibilityRepository.save(compatibility);
        return ProductMapper.toAdminCompatibilityResponse(saved);
    }

    public void deleteProductCompatibility(UUID productId, UUID compatibilityId) {
        // Ensure product exists
        findProductById(productId);

        ProductCompatibility compatibility = compatibilityRepository
                .findByIdAndProductId(compatibilityId, productId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "PRODUCT_COMPATIBILITY_NOT_FOUND",
                                "Ürün uyumluluk kaydı bulunamadı."
                        )
                );

        compatibilityRepository.delete(compatibility);
    }

    private Product findProductById(UUID id) {
        return productRepository
                .findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "PRODUCT_NOT_FOUND",
                                "Ürün bulunamadı."
                        )
                );
    }

    private String resolveSlug(String providedSlug, String name) {
        String candidate = (providedSlug != null && !providedSlug.isBlank())
                ? providedSlug
                : name;

        String slug = SlugUtils.toSlug(candidate);
        if (slug.isBlank()) {
            throw new BusinessException(
                    "VALIDATION_ERROR",
                    "Geçerli bir ürün slug değeri oluşturulamadı."
            );
        }

        return slug;
    }

    private void validatePriceRange(BigDecimal basePrice, BigDecimal salePrice) {
        if (salePrice != null && salePrice.compareTo(basePrice) > 0) {
            throw new BusinessException(
                    "INVALID_SALE_PRICE",
                    "İndirimli fiyat normal fiyattan büyük olamaz."
            );
        }
    }
}
