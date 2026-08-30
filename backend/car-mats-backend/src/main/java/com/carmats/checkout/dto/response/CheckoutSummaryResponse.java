package com.carmats.checkout.dto.response;

import com.carmats.user.dto.response.AddressResponse;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutSummaryResponse(
        List<CheckoutItemDto> items,
        int totalQuantity,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        boolean freeShippingApplied,
        BigDecimal freeShippingThreshold,
        BigDecimal discountTotal,
        BigDecimal grandTotal,
        AddressResponse deliveryAddress,
        AddressResponse billingAddress
) {
}
