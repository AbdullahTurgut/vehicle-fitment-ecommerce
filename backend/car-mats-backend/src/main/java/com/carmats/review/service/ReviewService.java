package com.carmats.review.service;

import com.carmats.catalog.entity.Product;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.common.response.PageResponse;
import com.carmats.order.entity.Order;
import com.carmats.order.entity.OrderItem;
import com.carmats.order.entity.OrderStatus;
import com.carmats.order.repository.OrderItemRepository;
import com.carmats.review.dto.request.CreateReviewRequest;
import com.carmats.review.dto.response.ProductReviewSummaryResponse;
import com.carmats.review.dto.response.ReviewResponse;
import com.carmats.review.entity.ProductReview;
import com.carmats.review.entity.ReviewStatus;
import com.carmats.review.mapper.ReviewMapper;
import com.carmats.review.repository.ProductReviewRepository;
import com.carmats.user.entity.User;
import com.carmats.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public ReviewService(
            ProductReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public ReviewResponse createReview(UUID userId, UUID productId, CreateReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Kullanıcı bulunamadı."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Ürün bulunamadı."));

        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new BusinessException("REVIEW_ALREADY_EXISTS", "Bu ürün için zaten bir değerlendirme yaptınız.");
        }

        List<OrderItem> purchasedItems = orderItemRepository.findAllByProductId(productId);
        Order matchedOrder = purchasedItems.stream()
                .map(OrderItem::getOrder)
                .filter(o -> o != null && o.getUser() != null && o.getUser().getId().equals(userId))
                .filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.DELIVERED)
                .findFirst()
                .orElse(null);

        boolean isVerified = matchedOrder != null;
        ReviewStatus status = isVerified ? ReviewStatus.APPROVED : ReviewStatus.PENDING;

        ProductReview review = new ProductReview(
                product,
                user,
                matchedOrder,
                request.rating(),
                request.title(),
                request.comment(),
                isVerified,
                status
        );

        ProductReview saved = reviewRepository.save(review);
        return ReviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductReviewSummaryResponse getProductReviews(UUID productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("PRODUCT_NOT_FOUND", "Ürün bulunamadı.");
        }

        Page<ProductReview> page = reviewRepository.findAllByProductIdAndStatusOrderByCreatedAtDesc(
                productId, ReviewStatus.APPROVED, pageable
        );

        Double avg = reviewRepository.calculateAverageRating(productId, ReviewStatus.APPROVED);
        double averageRating = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
        long totalReviews = reviewRepository.countByProductIdAndStatus(productId, ReviewStatus.APPROVED);

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, reviewRepository.countByProductIdAndStatusAndRating(productId, ReviewStatus.APPROVED, i));
        }

        return new ProductReviewSummaryResponse(
                averageRating,
                totalReviews,
                distribution,
                PageResponse.from(page.map(ReviewMapper::toResponse))
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getAllReviews(ReviewStatus status, Pageable pageable) {
        Page<ProductReview> page = reviewRepository.findAllByStatusFilter(status, pageable);
        return PageResponse.from(page.map(ReviewMapper::toResponse));
    }

    public ReviewResponse updateReviewStatus(UUID reviewId, ReviewStatus status) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("REVIEW_NOT_FOUND", "Değerlendirme bulunamadı."));

        review.setStatus(status);
        ProductReview saved = reviewRepository.save(review);
        return ReviewMapper.toResponse(saved);
    }
}
