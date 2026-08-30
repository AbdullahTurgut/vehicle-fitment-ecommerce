package com.carmats.checkout.dto.request;

import java.util.UUID;

public record CheckoutPreviewRequest(
        UUID deliveryAddressId,
        UUID billingAddressId,
        String couponCode,
        String guestToken
) {
}
