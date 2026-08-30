package com.carmats.review.controller;

import com.carmats.config.security.CustomUserDetails;
import com.carmats.review.dto.request.CreateReviewRequest;
import com.carmats.review.dto.response.ProductReviewSummaryResponse;
import com.carmats.review.dto.response.ReviewResponse;
import com.carmats.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID productId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        ReviewResponse response = reviewService.createReview(userDetails.getId(), productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ProductReviewSummaryResponse> getProductReviews(
            @PathVariable UUID productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ProductReviewSummaryResponse response = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(response);
    }
}
