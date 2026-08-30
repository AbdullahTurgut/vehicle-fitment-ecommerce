package com.carmats.catalog.service;

import com.carmats.catalog.dto.response.ProductListResponse;
import com.carmats.catalog.repository.CategoryRepository;
import com.carmats.catalog.repository.ProductCompatibilityRepository;
import com.carmats.catalog.repository.ProductFeatureRepository;
import com.carmats.catalog.repository.ProductImageRepository;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.catalog.repository.projection.ProductListProjection;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.vehicle.entity.VehicleGeneration;
import com.carmats.vehicle.entity.VehicleVariant;
import com.carmats.vehicle.repository.VehicleVariantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository imageRepository;

    @Mock
    private ProductFeatureRepository featureRepository;

    @Mock
    private ProductCompatibilityRepository compatibilityRepository;

    @Mock
    private VehicleVariantRepository vehicleVariantRepository;

    @InjectMocks
    private CatalogService catalogService;

    private static ProductListProjection createProjection(
            UUID id,
            String name,
            String slug,
            String sku,
            BigDecimal basePrice,
            BigDecimal salePrice,
            Integer stockQuantity,
            Boolean featured,
            String primaryImageUrl
    ) {
        return new ProductListProjection() {
            @Override
            public UUID getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getSlug() {
                return slug;
            }

            @Override
            public String getSku() {
                return sku;
            }

            @Override
            public BigDecimal getBasePrice() {
                return basePrice;
            }

            @Override
            public BigDecimal getSalePrice() {
                return salePrice;
            }

            @Override
            public Integer getStockQuantity() {
                return stockQuantity;
            }

            @Override
            public Boolean getFeatured() {
                return featured;
            }

            @Override
            public String getPrimaryImageUrl() {
                return primaryImageUrl;
            }
        };
    }

    private VehicleVariant createMockVariant(Integer startYear, Integer endYear) {
        VehicleGeneration generation = new VehicleGeneration(
                null,
                "B8",
                "B8",
                startYear,
                endYear
        );

        return new VehicleVariant(
                generation,
                "Standard"
        );
    }

    @Test
    @DisplayName("Compatible products are returned with primary images without triggering image repository queries")
    void shouldReturnCompatibleProductsWithoutQueryingImageRepository() {
        UUID variantId = UUID.randomUUID();
        VehicleVariant variant = createMockVariant(2015, 2024);

        when(vehicleVariantRepository.findByIdAndActiveTrue(variantId))
                .thenReturn(Optional.of(variant));

        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();

        ProductListProjection proj1 = createProjection(
                productId1,
                "Passat B8 Paspas",
                "passat-b8-paspas",
                "SKU-1",
                new BigDecimal("2499.90"),
                new BigDecimal("2249.90"),
                25,
                true,
                "/images/passat-paspas.jpg"
        );

        ProductListProjection proj2 = createProjection(
                productId2,
                "Passat B8 Bagaj",
                "passat-b8-bagaj",
                "SKU-2",
                new BigDecimal("1499.90"),
                null,
                10,
                false,
                null
        );

        when(compatibilityRepository.findCompatibleProducts(variantId, 2020))
                .thenReturn(List.of(proj1, proj2));

        List<ProductListResponse> results = catalogService.getCompatibleProducts(variantId, 2020);

        assertThat(results).hasSize(2);

        ProductListResponse first = results.get(0);
        assertThat(first.id()).isEqualTo(productId1);
        assertThat(first.name()).isEqualTo("Passat B8 Paspas");
        assertThat(first.primaryImageUrl()).isEqualTo("/images/passat-paspas.jpg");
        assertThat(first.effectivePrice()).isEqualByComparingTo("2249.90");
        assertThat(first.inStock()).isTrue();
        assertThat(first.featured()).isTrue();

        ProductListResponse second = results.get(1);
        assertThat(second.id()).isEqualTo(productId2);
        assertThat(second.name()).isEqualTo("Passat B8 Bagaj");
        assertThat(second.primaryImageUrl()).isNull();
        assertThat(second.effectivePrice()).isEqualByComparingTo("1499.90");
        assertThat(second.inStock()).isTrue();
        assertThat(second.featured()).isFalse();

        // Verify imageRepository is never called (N+1 query eliminated)
        verifyNoInteractions(imageRepository);
    }

    @Test
    @DisplayName("Should allow null year parameter")
    void shouldAllowNullYear() {
        UUID variantId = UUID.randomUUID();
        VehicleVariant variant = createMockVariant(2015, 2024);

        when(vehicleVariantRepository.findByIdAndActiveTrue(variantId))
                .thenReturn(Optional.of(variant));

        when(compatibilityRepository.findCompatibleProducts(variantId, null))
                .thenReturn(List.of());

        List<ProductListResponse> results = catalogService.getCompatibleProducts(variantId, null);

        assertThat(results).isEmpty();
        verify(compatibilityRepository).findCompatibleProducts(variantId, null);
        verifyNoInteractions(imageRepository);
    }

    @Test
    @DisplayName("Should throw BusinessException when vehicle year is before generation start year")
    void shouldThrowWhenYearBeforeStartYear() {
        UUID variantId = UUID.randomUUID();
        VehicleVariant variant = createMockVariant(2015, 2024);

        when(vehicleVariantRepository.findByIdAndActiveTrue(variantId))
                .thenReturn(Optional.of(variant));

        assertThatThrownBy(() -> catalogService.getCompatibleProducts(variantId, 2010))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_VEHICLE_YEAR");

        verify(compatibilityRepository, never()).findCompatibleProducts(any(), any());
    }

    @Test
    @DisplayName("Should throw BusinessException when vehicle year is after generation end year")
    void shouldThrowWhenYearAfterEndYear() {
        UUID variantId = UUID.randomUUID();
        VehicleVariant variant = createMockVariant(2015, 2024);

        when(vehicleVariantRepository.findByIdAndActiveTrue(variantId))
                .thenReturn(Optional.of(variant));

        assertThatThrownBy(() -> catalogService.getCompatibleProducts(variantId, 2025))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_VEHICLE_YEAR");

        verify(compatibilityRepository, never()).findCompatibleProducts(any(), any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when vehicle variant is not found")
    void shouldThrowWhenVariantNotFound() {
        UUID variantId = UUID.randomUUID();

        when(vehicleVariantRepository.findByIdAndActiveTrue(variantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.getCompatibleProducts(variantId, 2020))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", "VEHICLE_VARIANT_NOT_FOUND");

        verify(compatibilityRepository, never()).findCompatibleProducts(any(), any());
    }
}
