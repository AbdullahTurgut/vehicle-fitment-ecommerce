package com.carmats.catalog.dto.request;

import com.carmats.catalog.entity.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(
        @NotNull(message = "Ürün durumu belirtilmelidir.")
        ProductStatus status
) {
}
