package com.carmats.catalog.controller;

import com.carmats.catalog.dto.response.CategoryResponse;
import com.carmats.catalog.dto.response.ProductDetailResponse;
import com.carmats.catalog.dto.response.ProductListResponse;
import com.carmats.catalog.service.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(
            CatalogService catalogService
    ) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {

        return ResponseEntity.ok(
                catalogService.getCategories()
        );
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductListResponse>> getProducts() {

        return ResponseEntity.ok(
                catalogService.getProducts()
        );
    }

    @GetMapping("/products/{slug}")
    public ResponseEntity<ProductDetailResponse> getProduct(
            @PathVariable String slug
    ) {

        return ResponseEntity.ok(
                catalogService.getProductBySlug(slug)
        );
    }

    @GetMapping("/compatible-products")
    public ResponseEntity<List<ProductListResponse>> getCompatibleProducts(
            @RequestParam UUID variantId,
            @RequestParam(required = false) Integer year
    ) {

        return ResponseEntity.ok(
                catalogService.getCompatibleProducts(
                        variantId,
                        year
                )
        );
    }
}