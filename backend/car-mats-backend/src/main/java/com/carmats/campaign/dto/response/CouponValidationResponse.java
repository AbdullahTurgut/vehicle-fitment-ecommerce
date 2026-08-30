package com.carmats.campaign.dto.response;

import java.math.BigDecimal;

public record CouponValidationResponse(
        boolean valid,
        String message,
        String code,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        CouponResponse coupon
) {
    public static CouponValidationResponse valid(
            String code,
            BigDecimal discountAmount,
            BigDecimal finalAmount,
            CouponResponse coupon
    ) {
        return new CouponValidationResponse(
                true,
                "Kupon başarıyla uygulandı.",
                code,
                discountAmount,
                finalAmount,
                coupon
        );
    }

    public static CouponValidationResponse invalid(String message) {
        return new CouponValidationResponse(
                false,
                message,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null
        );
    }
}
