package com.carmats.catalog.service;

import com.carmats.catalog.dto.request.*;
import com.carmats.catalog.dto.response.*;
import com.carmats.catalog.entity.*;
import com.carmats.catalog.repository.*;
import com.carmats.common.response.PageResponse;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.vehicle.entity.VehicleBrand;
import com.carmats.vehicle.entity.VehicleGeneration;
import com.carmats.vehicle.entity.VehicleModel;
import com.carmats.vehicle.entity.VehicleVariant;
import com.carmats.vehicle.repository.VehicleVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductImageRepository imageRepository;

    @Mock
    private ProductFeatureRepository featureRepository;

    @Mock
    private ProductCompatibilityRepository compatibilityRepository;

    @Mock
    private VehicleVariantRepository vehicleVariantRepository;

    @InjectMocks
    private AdminProductService adminProductService;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = new Category("3D Paspas", "3d-paspas");
        testProduct = new Product(
                testCategory,
                "Passat Paspas",
                "passat-paspas",
                "SKU-PASSAT-01",
                "Kısa açıklama",
                "Detaylı açıklama",
                new BigDecimal("1200.00"),
                new BigDecimal("990.00"),
                50,
                ProductStatus.ACTIVE,
                true,
                "Sahler",
                "TPE"
        );
    }

    @Test
    @DisplayName("Should return paginated admin products with primary image")
    void shouldReturnPaginatedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findAdminProducts(null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(testProduct), pageable, 1));

        ProductImage primaryImg = new ProductImage(testProduct, "/primary.jpg", "", 0, true);
        when(imageRepository.findAllByProductIdOrderBySortOrderAsc(testProduct.getId()))
                .thenReturn(List.of(primaryImg));

        PageResponse<AdminProductListResponse> response = adminProductService.getProducts(null, null, null, pageable);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).name()).isEqualTo("Passat Paspas");
        assertThat(response.content().get(0).primaryImageUrl()).isEqualTo("/primary.jpg");
        assertThat(response.content().get(0).effectivePrice()).isEqualByComparingTo("990.00");
    }

    @Test
    @DisplayName("Should return full product detail by id")
    void shouldReturnProductDetailById() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.of(testProduct));
        when(imageRepository.findAllByProductIdOrderBySortOrderAsc(id)).thenReturn(List.of());
        when(featureRepository.findAllByProductIdOrderBySortOrderAsc(id)).thenReturn(List.of());
        when(compatibilityRepository.findAllByProductId(id)).thenReturn(List.of());

        AdminProductDetailResponse response = adminProductService.getProductById(id);

        assertThat(response.name()).isEqualTo("Passat Paspas");
        assertThat(response.sku()).isEqualTo("SKU-PASSAT-01");
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getting non-existent product")
    void shouldThrowWhenProductNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.getProductById(id))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "PRODUCT_NOT_FOUND");
    }

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {
        UUID categoryId = UUID.randomUUID();
        CreateProductRequest request = new CreateProductRequest(
                categoryId,
                "Yeni Golf Paspas",
                "yeni-golf-paspas",
                "SKU-GOLF-01",
                "Kısa açıklama",
                "Uzun açıklama",
                new BigDecimal("1500.00"),
                new BigDecimal("1200.00"),
                100,
                ProductStatus.ACTIVE,
                true,
                "Sahler",
                "Kauçuk"
        );

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(productRepository.existsBySlug("yeni-golf-paspas")).thenReturn(false);
        when(productRepository.existsBySku("SKU-GOLF-01")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminProductDetailResponse response = adminProductService.createProduct(request);

        assertThat(response.name()).isEqualTo("Yeni Golf Paspas");
        assertThat(response.slug()).isEqualTo("yeni-golf-paspas");
        assertThat(response.sku()).isEqualTo("SKU-GOLF-01");
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should throw BusinessException when sale price is higher than base price")
    void shouldThrowWhenSalePriceHigherThanBasePrice() {
        UUID categoryId = UUID.randomUUID();
        CreateProductRequest request = new CreateProductRequest(
                categoryId,
                "Paspas",
                null,
                "SKU-01",
                null,
                null,
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                10,
                null,
                null,
                null,
                null
        );

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(productRepository.existsBySlug("paspas")).thenReturn(false);
        when(productRepository.existsBySku("SKU-01")).thenReturn(false);

        assertThatThrownBy(() -> adminProductService.createProduct(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_SALE_PRICE");
    }

    @Test
    @DisplayName("Should throw BusinessException when creating product with duplicate SKU")
    void shouldThrowWhenDuplicateSkuOnCreate() {
        UUID categoryId = UUID.randomUUID();
        CreateProductRequest request = new CreateProductRequest(
                categoryId,
                "Golf Paspas",
                null,
                "SKU-DUPLICATE",
                null,
                null,
                new BigDecimal("1000.00"),
                null,
                10,
                null,
                null,
                null,
                null
        );

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(productRepository.existsBySlug("golf-paspas")).thenReturn(false);
        when(productRepository.existsBySku("SKU-DUPLICATE")).thenReturn(true);

        assertThatThrownBy(() -> adminProductService.createProduct(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "PRODUCT_SKU_ALREADY_EXISTS");
    }

    @Test
    @DisplayName("Should add image and unset previous primary image if new image is primary")
    void shouldAddPrimaryImageAndUnsetExisting() {
        UUID productId = UUID.randomUUID();
        ProductImage existingPrimary = new ProductImage(testProduct, "/old.jpg", "eski", 0, true);

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(imageRepository.findAllByProductIdOrderBySortOrderAsc(productId))
                .thenReturn(List.of(existingPrimary));
        when(imageRepository.save(any(ProductImage.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateProductImageRequest request = new CreateProductImageRequest(
                "/new-primary.jpg",
                "yeni",
                1,
                true
        );

        ProductImageResponse response = adminProductService.addProductImage(productId, request);

        assertThat(response.url()).isEqualTo("/new-primary.jpg");
        assertThat(response.primary()).isTrue();
        assertThat(existingPrimary.isPrimary()).isFalse();
    }

    @Test
    @DisplayName("Should add feature to product")
    void shouldAddFeatureToProduct() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(featureRepository.save(any(ProductFeature.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateProductFeatureRequest request = new CreateProductFeatureRequest(
                "Kokusuz TPE",
                "Geri dönüştürülebilir malzeme",
                "eco-icon",
                1
        );

        ProductFeatureResponse response = adminProductService.addProductFeature(productId, request);

        assertThat(response.title()).isEqualTo("Kokusuz TPE");
        assertThat(response.icon()).isEqualTo("eco-icon");
    }

    @Test
    @DisplayName("Should add compatibility to product and validate year bounds")
    void shouldAddCompatibilityToProduct() {
        UUID productId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();

        VehicleBrand brand = new VehicleBrand("Volkswagen", "volkswagen", null, true, 1);
        VehicleModel model = new VehicleModel(brand, "Passat", "passat");
        VehicleGeneration gen = new VehicleGeneration(model, "B8", "b8", 2015, 2024);
        VehicleVariant variant = new VehicleVariant(gen, "Sedan");

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(vehicleVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));
        when(compatibilityRepository.existsByProductIdAndVehicleVariantIdAndStartYearAndEndYear(
                productId, variantId, 2016, 2020
        )).thenReturn(false);
        when(compatibilityRepository.save(any(ProductCompatibility.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateProductCompatibilityRequest request = new CreateProductCompatibilityRequest(
                variantId,
                2016,
                2020,
                "Tüm donanım paketleri"
        );

        AdminProductCompatibilityResponse response = adminProductService.addProductCompatibility(productId, request);

        assertThat(response.variantName()).isEqualTo("Sedan");
        assertThat(response.modelName()).isEqualTo("Passat");
        assertThat(response.brandName()).isEqualTo("Volkswagen");
        assertThat(response.startYear()).isEqualTo(2016);
        assertThat(response.endYear()).isEqualTo(2020);
    }

    @Test
    @DisplayName("Should throw BusinessException when compatibility year is outside generation boundary")
    void shouldThrowWhenCompatibilityYearOutsideGenBoundary() {
        UUID productId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();

        VehicleBrand brand = new VehicleBrand("Volkswagen", "volkswagen", null, true, 1);
        VehicleModel model = new VehicleModel(brand, "Passat", "passat");
        VehicleGeneration gen = new VehicleGeneration(model, "B8", "b8", 2015, 2024);
        VehicleVariant variant = new VehicleVariant(gen, "Sedan");

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(vehicleVariantRepository.findById(variantId)).thenReturn(Optional.of(variant));

        CreateProductCompatibilityRequest request = new CreateProductCompatibilityRequest(
                variantId,
                2010, // Invalid: Passat B8 started in 2015
                2020,
                null
        );

        assertThatThrownBy(() -> adminProductService.addProductCompatibility(productId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_VEHICLE_YEAR");
    }
}
