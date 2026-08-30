package com.carmats.catalog.controller;

import com.carmats.catalog.dto.request.CreateCategoryRequest;
import com.carmats.catalog.dto.request.UpdateCategoryRequest;
import com.carmats.catalog.dto.request.UpdateCategoryStatusRequest;
import com.carmats.catalog.dto.response.AdminCategoryResponse;
import com.carmats.catalog.service.AdminCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Admin Kategori",
        description = "Admin kategori yönetim işlemleri"
)
@RestController
@RequestMapping("/api/v1/admin/categories")
@Validated
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @Operation(
            summary = "Tüm kategorileri listeler",
            description = "Admin için aktif ve pasif tüm kategorileri listeler."
    )
    @GetMapping
    public ResponseEntity<List<AdminCategoryResponse>> getCategories() {
        return ResponseEntity.ok(adminCategoryService.getCategories());
    }

    @Operation(
            summary = "Kategori detayını getirir",
            description = "ID değerine göre kategori detayını getirir."
    )
    @GetMapping("/{id}")
    public ResponseEntity<AdminCategoryResponse> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminCategoryService.getCategoryById(id));
    }

    @Operation(
            summary = "Yeni kategori oluşturur",
            description = "Yeni bir kategori kaydı oluşturur."
    )
    @PostMapping
    public ResponseEntity<AdminCategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        AdminCategoryResponse response = adminCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Kategori bilgilerini günceller",
            description = "Mevcut kategorinin bilgilerini günceller."
    )
    @PutMapping("/{id}")
    public ResponseEntity<AdminCategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return ResponseEntity.ok(adminCategoryService.updateCategory(id, request));
    }

    @Operation(
            summary = "Kategori aktiflik durumunu günceller",
            description = "Kategoriyi aktif veya pasif duruma getirir."
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminCategoryResponse> updateCategoryStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryStatusRequest request
    ) {
        return ResponseEntity.ok(adminCategoryService.updateCategoryStatus(id, request));
    }
}
