package com.carmats.catalog.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateProductCompatibilityRequest(
        @NotNull(message = "Araç varyantı seçilmelidir.")
        UUID vehicleVariantId,

        @Min(value = 1900, message = "Başlangıç yılı 1900 veya daha büyük olmalıdır.")
        @Max(value = 2100, message = "Başlangıç yılı 2100 veya daha küçük olmalıdır.")
        Integer startYear,

        @Min(value = 1900, message = "Bitiş yılı 1900 veya daha büyük olmalıdır.")
        @Max(value = 2100, message = "Bitiş yılı 2100 veya daha küçük olmalıdır.")
        Integer endYear,

        String notes
) {
}
