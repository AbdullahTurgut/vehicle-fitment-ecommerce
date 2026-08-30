package com.carmats.cart.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MergeCartRequest(
        @NotBlank(message = "Misafir sepet token'ı boş olamaz.")
        String guestToken
) {
}
