package com.carmats.catalog.controller;

import com.carmats.catalog.dto.response.CategoryResponse;
import com.carmats.catalog.dto.response.ProductDetailResponse;
import com.carmats.catalog.dto.response.ProductListResponse;
import com.carmats.catalog.service.CatalogService;
import com.carmats.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Tag(
        name = "Katalog",
        description = "Ürün ve kategori işlemleri"
)
@RestController
@RequestMapping("/api/v1/catalog")
@Validated
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * Aktif kategorileri listeler.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {

        return ResponseEntity.ok(
                catalogService.getCategories()
        );
    }

    /**
     * Aktif ürünleri sayfalı olarak listeler.
     * Opsiyonel olarak kategori slug değeri ile filtreleme yapılabilir.
     *
     * Örnek:
     * /api/v1/catalog/products?page=0&size=12
     * /api/v1/catalog/products?page=0&size=12&category=3d-oto-paspas
     */

    @Operation(
            summary = "Ürünleri listeler",
            description =
                    "Aktif ürünleri sayfalı şekilde listeler. " +
                            "Kategori slug bilgisi ile filtreleme yapılabilir."
    )
    @GetMapping("/products")
    public ResponseEntity<PageResponse<ProductListResponse>> getProducts(

            @RequestParam(defaultValue = "0")
            @Min(
                    value = 0,
                    message = "Sayfa numarası 0 veya daha büyük olmalıdır."
            )
            int page,

            @RequestParam(defaultValue = "12")
            @Min(
                    value = 1,
                    message = "Sayfa boyutu en az 1 olmalıdır."
            )
            @Max(
                    value = 48,
                    message = "Sayfa boyutu en fazla 48 olabilir."
            )
            int size,

            @RequestParam(required = false)
            String category
    ) {

        return ResponseEntity.ok(
                catalogService.getProducts(
                        category,
                        page,
                        size
                )
        );
    }

    /**
     * Slug değerine göre aktif ürün detayını getirir.
     *
     * Örnek:
     * /api/v1/catalog/products/volkswagen-passat-b8-3d-havuzlu-paspas
     */
    @Operation(
            summary = "Ürün detayını getirir",
            description =
                    "Slug değerine göre aktif ürün detayını getirir."
    )
    @GetMapping("/products/{slug}")
    public ResponseEntity<ProductDetailResponse> getProduct(
            @PathVariable String slug
    ) {

        return ResponseEntity.ok(
                catalogService.getProductBySlug(slug)
        );
    }

    /**
     * Araç varyantı ve model yılına göre uyumlu ürünleri getirir.
     *
     * Örnek:
     * /api/v1/catalog/compatible-products
     * ?variantId=cccccccc-cccc-cccc-cccc-cccccccccccc
     * &year=2021
     */
    @Operation(
            summary = "Araca uyumlu ürünleri getirir",
            description =
                    "Araç varyantı ve opsiyonel model yılına göre uyumlu ürünleri listeler."
    )
    @GetMapping("/compatible-products")
    public ResponseEntity<List<ProductListResponse>> getCompatibleProducts(

            @RequestParam
            UUID variantId,

            @RequestParam(required = false)
            @Min(
                    value = 1950,
                    message = "Araç yılı 1950 veya daha büyük olmalıdır."
            )
            @Max(
                    value = 2100,
                    message = "Araç yılı geçerli bir değer olmalıdır."
            )
            Integer year
    ) {

        return ResponseEntity.ok(
                catalogService.getCompatibleProducts(
                        variantId,
                        year
                )
        );
    }
}