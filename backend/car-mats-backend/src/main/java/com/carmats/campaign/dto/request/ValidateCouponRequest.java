package com.carmats.campaign.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ValidateCouponRequest(
        @NotBlank(message = "Kupon kodu zorunludur.")
        String code,

        @NotNull(message = "Sepet tutarı zorunludur.")
        @DecimalMin(value = "0.01", message = "Sepet tutarı 0'dan büyük olmalıdır.")
        BigDecimal cartSubtotal
) {
}
