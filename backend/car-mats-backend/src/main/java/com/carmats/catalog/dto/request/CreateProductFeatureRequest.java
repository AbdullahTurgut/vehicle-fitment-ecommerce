package com.carmats.catalog.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductFeatureRequest(
        @NotBlank(message = "Özellik başlığı boş olamaz.")
        @Size(max = 150, message = "Özellik başlığı en fazla 150 karakter olabilir.")
        String title,

        @Size(max = 500, message = "Özellik açıklaması en fazla 500 karakter olabilir.")
        String description,

        @Size(max = 100, message = "İkon bilgisi en fazla 100 karakter olabilir.")
        String icon,

        @Min(value = 0, message = "Sıralama değeri 0 veya daha büyük olmalıdır.")
        Integer sortOrder
) {
}
