package com.carmats.review.mapper;

import com.carmats.review.dto.response.ReviewResponse;
import com.carmats.review.entity.ProductReview;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewResponse toResponse(ProductReview review) {
        if (review == null) {
            return null;
        }

        String userName = review.getUser() != null
                ? (review.getUser().getFirstName() + " " + (review.getUser().getLastName() != null ? review.getUser().getLastName().substring(0, 1) + "." : ""))
                : "Anonim";

        return new ReviewResponse(
                review.getId(),
                review.getProduct() != null ? review.getProduct().getId() : null,
                review.getProduct() != null ? review.getProduct().getName() : null,
                review.getUser() != null ? review.getUser().getId() : null,
                userName,
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.isVerifiedPurchase(),
                review.getStatus(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
