package com.carmats.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddToCartRequest(
        @NotNull(message = "Ürün ID boş olamaz.")
        UUID productId,

        UUID vehicleVariantId,

        @NotNull(message = "Adet boş olamaz.")
        @Min(value = 1, message = "Adet en az 1 olmalıdır.")
        Integer quantity
) {
}
