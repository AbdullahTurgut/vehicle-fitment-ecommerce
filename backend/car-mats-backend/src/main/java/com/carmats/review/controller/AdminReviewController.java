package com.carmats.review.controller;

import com.carmats.common.response.PageResponse;
import com.carmats.review.dto.response.ReviewResponse;
import com.carmats.review.entity.ReviewStatus;
import com.carmats.review.service.ReviewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ReviewResponse>> getAllReviews(
            @RequestParam(required = false) ReviewStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<ReviewResponse> response = reviewService.getAllReviews(status, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{reviewId}/status")
    public ResponseEntity<ReviewResponse> updateReviewStatus(
            @PathVariable UUID reviewId,
            @RequestBody Map<String, String> body
    ) {
        String statusStr = body.getOrDefault("status", "APPROVED");
        ReviewStatus status = ReviewStatus.valueOf(statusStr.toUpperCase());
        ReviewResponse response = reviewService.updateReviewStatus(reviewId, status);
        return ResponseEntity.ok(response);
    }
}
