package com.carmats.catalog.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductImageRequest(
        @NotBlank(message = "Görsel URL boş olamaz.")
        @Size(max = 500, message = "Görsel URL en fazla 500 karakter olabilir.")
        String url,

        @Size(max = 250, message = "Alt metin en fazla 250 karakter olabilir.")
        String altText,

        @Min(value = 0, message = "Sıralama değeri 0 veya daha büyük olmalıdır.")
        Integer sortOrder,

        Boolean primary
) {
}
