package com.carmats.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemQuantityRequest(
        @NotNull(message = "Adet boş olamaz.")
        @Min(value = 1, message = "Adet en az 1 olmalıdır.")
        Integer quantity
) {
}
