package com.carmats.catalog.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRequest(
        UUID parentId,

        @NotBlank(message = "Kategori adı boş olamaz.")
        @Size(max = 120, message = "Kategori adı en fazla 120 karakter olabilir.")
        String name,

        @Size(max = 150, message = "Kategori slug en fazla 150 karakter olabilir.")
        String slug,

        String description,

        @Size(max = 500, message = "Görsel URL en fazla 500 karakter olabilir.")
        String imageUrl,

        Boolean active,

        @Min(value = 0, message = "Sıralama değeri 0 veya daha büyük olmalıdır.")
        Integer sortOrder
) {
}
