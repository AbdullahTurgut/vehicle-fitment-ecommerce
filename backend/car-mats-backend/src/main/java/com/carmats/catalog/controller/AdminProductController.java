package com.carmats.catalog.controller;

import com.carmats.catalog.dto.request.*;
import com.carmats.catalog.dto.response.*;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.service.AdminProductService;
import com.carmats.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Admin Ürün",
        description = "Admin ürün, görsel, özellik ve uyumluluk yönetim işlemleri"
)
@RestController
@RequestMapping("/api/v1/admin/products")
@Validated
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @Operation(
            summary = "Ürünleri listeler",
            description = "Admin için filtrelenebilir ve sayfalanabilir ürün listesi."
    )
    @GetMapping
    public ResponseEntity<PageResponse<AdminProductListResponse>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Sayfa numarası 0 veya daha büyük olmalıdır.") int page,
            @RequestParam(defaultValue = "12") @Min(value = 1, message = "Sayfa boyutu en az 1 olmalıdır.") @Max(value = 100, message = "Sayfa boyutu en fazla 100 olabilir.") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminProductService.getProducts(search, categoryId, status, pageable));
    }

    @Operation(
            summary = "Ürün detayını getirir",
            description = "ID değerine göre ürün detayını, görsellerini, özelliklerini ve araç uyumluluklarını getirir."
    )
    @GetMapping("/{id}")
    public ResponseEntity<AdminProductDetailResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminProductService.getProductById(id));
    }

    @Operation(
            summary = "Yeni ürün oluşturur",
            description = "Yeni bir ürün kaydı oluşturur."
    )
    @PostMapping
    public ResponseEntity<AdminProductDetailResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        AdminProductDetailResponse response = adminProductService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Ürün bilgilerini günceller",
            description = "Mevcut ürünün temel ve fiyat/stok bilgilerini günceller."
    )
    @PutMapping("/{id}")
    public ResponseEntity<AdminProductDetailResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ResponseEntity.ok(adminProductService.updateProduct(id, request));
    }

    @Operation(
            summary = "Ürün durumunu günceller",
            description = "Ürünün durumunu (DRAFT, ACTIVE, PASSIVE, OUT_OF_STOCK) günceller."
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminProductDetailResponse> updateProductStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductStatusRequest request
    ) {
        return ResponseEntity.ok(adminProductService.updateProductStatus(id, request));
    }

    @Operation(
            summary = "Ürüne görsel ekler",
            description = "Ürüne yeni görsel ekler. Primary olarak işaretlenirse diğer birincil görsel unprimary yapılır."
    )
    @PostMapping("/{id}/images")
    public ResponseEntity<ProductImageResponse> addProductImage(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProductImageRequest request
    ) {
        ProductImageResponse response = adminProductService.addProductImage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Ürün görselini siler",
            description = "Ürüne ait belirtilen görseli siler."
    )
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable UUID id,
            @PathVariable UUID imageId
    ) {
        adminProductService.deleteProductImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Ürüne özellik ekler",
            description = "Ürüne yeni özellik (başlık, açıklama, ikon) ekler."
    )
    @PostMapping("/{id}/features")
    public ResponseEntity<ProductFeatureResponse> addProductFeature(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProductFeatureRequest request
    ) {
        ProductFeatureResponse response = adminProductService.addProductFeature(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Ürün özelliğini siler",
            description = "Ürüne ait belirtilen özelliği siler."
    )
    @DeleteMapping("/{id}/features/{featureId}")
    public ResponseEntity<Void> deleteProductFeature(
            @PathVariable UUID id,
            @PathVariable UUID featureId
    ) {
        adminProductService.deleteProductFeature(id, featureId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Ürüne araç uyumluluğu ekler",
            description = "Ürünü bir araç varyantı ve model yılı aralığı ile eşleştirir."
    )
    @PostMapping("/{id}/compatibilities")
    public ResponseEntity<AdminProductCompatibilityResponse> addProductCompatibility(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProductCompatibilityRequest request
    ) {
        AdminProductCompatibilityResponse response = adminProductService.addProductCompatibility(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Ürün araç uyumluluğunu siler",
            description = "Ürüne ait belirtilen araç uyumluluk kaydını siler."
    )
    @DeleteMapping("/{id}/compatibilities/{compatibilityId}")
    public ResponseEntity<Void> deleteProductCompatibility(
            @PathVariable UUID id,
            @PathVariable UUID compatibilityId
    ) {
        adminProductService.deleteProductCompatibility(id, compatibilityId);
        return ResponseEntity.noContent().build();
    }
}
