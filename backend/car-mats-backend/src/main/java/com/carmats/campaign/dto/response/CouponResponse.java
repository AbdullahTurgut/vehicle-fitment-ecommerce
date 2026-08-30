package com.carmats.campaign.dto.response;

import com.carmats.campaign.entity.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumOrderAmount,
        BigDecimal maxDiscountAmount,
        Integer usageLimit,
        int usageLimitPerUser,
        int usedCount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
