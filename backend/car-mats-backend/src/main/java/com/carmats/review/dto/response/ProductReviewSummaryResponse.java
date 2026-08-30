package com.carmats.review.dto.response;

import com.carmats.common.response.PageResponse;

import java.util.Map;

public record ProductReviewSummaryResponse(
        double averageRating,
        long totalReviews,
        Map<Integer, Long> ratingDistribution,
        PageResponse<ReviewResponse> reviews
) {
}
