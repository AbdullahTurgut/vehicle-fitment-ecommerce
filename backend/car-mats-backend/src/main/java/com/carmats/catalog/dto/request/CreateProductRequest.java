package com.carmats.catalog.dto.request;

import com.carmats.catalog.entity.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
        @NotNull(message = "Kategori seçilmelidir.")
        UUID categoryId,

        @NotBlank(message = "Ürün adı boş olamaz.")
        @Size(max = 200, message = "Ürün adı en fazla 200 karakter olabilir.")
        String name,

        @Size(max = 220, message = "Ürün slug en fazla 220 karakter olabilir.")
        String slug,

        @NotBlank(message = "SKU boş olamaz.")
        @Size(max = 100, message = "SKU en fazla 100 karakter olabilir.")
        String sku,

        @Size(max = 500, message = "Kısa açıklama en fazla 500 karakter olabilir.")
        String shortDescription,

        String description,

        @NotNull(message = "Fiyat boş olamaz.")
        @DecimalMin(value = "0.01", message = "Fiyat 0'dan büyük olmalıdır.")
        BigDecimal basePrice,

        @DecimalMin(value = "0.00", message = "İndirimli fiyat 0 veya daha büyük olmalıdır.")
        BigDecimal salePrice,

        @Min(value = 0, message = "Stok miktarı 0 veya daha büyük olmalıdır.")
        Integer stockQuantity,

        ProductStatus status,

        Boolean featured,

        @Size(max = 120, message = "Üretici marka en fazla 120 karakter olabilir.")
        String manufacturerBrand,

        @Size(max = 120, message = "Malzeme bilgisi en fazla 120 karakter olabilir.")
        String material
) {
}
