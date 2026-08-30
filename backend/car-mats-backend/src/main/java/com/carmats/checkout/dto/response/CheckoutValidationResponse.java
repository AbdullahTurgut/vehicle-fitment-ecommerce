package com.carmats.checkout.dto.response;

import java.util.List;

public record CheckoutValidationResponse(
        boolean valid,
        List<String> errors,
        List<String> warnings
) {
    public static CheckoutValidationResponse ok() {
        return new CheckoutValidationResponse(true, List.of(), List.of());
    }

    public static CheckoutValidationResponse fail(List<String> errors) {
        return new CheckoutValidationResponse(false, errors, List.of());
    }
}
