package com.carmats.campaign.dto.request;

import com.carmats.campaign.entity.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateCouponRequest(
        @NotBlank(message = "Kupon kodu zorunludur.")
        String code,

        String description,

        @NotNull(message = "İndirim türü zorunludur.")
        DiscountType discountType,

        @NotNull(message = "İndirim değeri zorunludur.")
        @DecimalMin(value = "0.01", message = "İndirim değeri 0'dan büyük olmalıdır.")
        BigDecimal discountValue,

        BigDecimal minimumOrderAmount,

        BigDecimal maxDiscountAmount,

        Integer usageLimit,

        Integer usageLimitPerUser,

        LocalDateTime startDate,

        LocalDateTime endDate,

        Boolean active
) {
}
