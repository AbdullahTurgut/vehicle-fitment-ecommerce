package com.carmats.review.repository;

import com.carmats.review.entity.ProductReview;
import com.carmats.review.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {

    Page<ProductReview> findAllByProductIdAndStatusOrderByCreatedAtDesc(UUID productId, ReviewStatus status, Pageable pageable);

    Optional<ProductReview> findByProductIdAndUserId(UUID productId, UUID userId);

    boolean existsByProductIdAndUserId(UUID productId, UUID userId);

    @Query("SELECT r FROM ProductReview r WHERE (:status IS NULL OR r.status = :status) ORDER BY r.createdAt DESC")
    Page<ProductReview> findAllByStatusFilter(@Param("status") ReviewStatus status, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.status = :status")
    Double calculateAverageRating(@Param("productId") UUID productId, @Param("status") ReviewStatus status);

    long countByProductIdAndStatus(UUID productId, ReviewStatus status);

    long countByProductIdAndStatusAndRating(UUID productId, ReviewStatus status, int rating);
}
