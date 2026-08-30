package com.carmats.review.dto.response;

import com.carmats.review.entity.ReviewStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID productId,
        String productName,
        UUID userId,
        String userName,
        int rating,
        String title,
        String comment,
        boolean isVerifiedPurchase,
        ReviewStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
