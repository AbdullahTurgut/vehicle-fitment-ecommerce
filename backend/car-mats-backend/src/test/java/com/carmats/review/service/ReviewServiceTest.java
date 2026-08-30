package com.carmats.review.service;

import com.carmats.catalog.entity.Product;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.common.exception.BusinessException;
import com.carmats.order.repository.OrderItemRepository;
import com.carmats.review.dto.request.CreateReviewRequest;
import com.carmats.review.dto.response.ProductReviewSummaryResponse;
import com.carmats.review.dto.response.ReviewResponse;
import com.carmats.review.entity.ProductReview;
import com.carmats.review.entity.ReviewStatus;
import com.carmats.review.repository.ProductReviewRepository;
import com.carmats.user.entity.User;
import com.carmats.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ProductReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Product testProduct;
    private UUID userId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        testUser = new User("reviewer@carmats.local", "pass", "Ali", "Kaya", "+905551112233");
        ReflectionTestUtils.setField(testUser, "id", userId);

        testProduct = new Product(
                null, "3D Havuzlu Paspas", "3d-havuzlu-paspas", "PAS-001",
                "Açıklama", "Kısa açıklama", null, null, 10, null, false, "TPE", "Sahler"
        );
        ReflectionTestUtils.setField(testProduct, "id", productId);
    }

    @Test
    @DisplayName("Should create review successfully with PENDING status for unverified purchase")
    void shouldCreateReviewSuccessfullyForUnverifiedUser() {
        CreateReviewRequest request = new CreateReviewRequest(5, "Harika ürün", "Birebir oturdu, tavsiye ederim.");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(reviewRepository.existsByProductIdAndUserId(productId, userId)).thenReturn(false);
        when(orderItemRepository.findAllByProductId(productId)).thenReturn(Collections.emptyList());
        when(reviewRepository.save(any(ProductReview.class))).thenAnswer(inv -> {
            ProductReview r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", UUID.randomUUID());
            return r;
        });

        ReviewResponse response = reviewService.createReview(userId, productId, request);

        assertThat(response).isNotNull();
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.status()).isEqualTo(ReviewStatus.PENDING);
        assertThat(response.isVerifiedPurchase()).isFalse();
        verify(reviewRepository).save(any(ProductReview.class));
    }

    @Test
    @DisplayName("Should reject duplicate review for same user and product")
    void shouldRejectDuplicateReview() {
        CreateReviewRequest request = new CreateReviewRequest(5, "Başlık", "Yorum");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(reviewRepository.existsByProductIdAndUserId(productId, userId)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(userId, productId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten bir değerlendirme yaptınız");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get product reviews with average rating and stats")
    void shouldGetProductReviewsWithStats() {
        ProductReview review = new ProductReview(
                testProduct, testUser, null, 5, "Mükemmel", "Çok iyi", true, ReviewStatus.APPROVED
        );
        Page<ProductReview> page = new PageImpl<>(List.of(review));

        when(productRepository.existsById(productId)).thenReturn(true);
        when(reviewRepository.findAllByProductIdAndStatusOrderByCreatedAtDesc(eq(productId), eq(ReviewStatus.APPROVED), any()))
                .thenReturn(page);
        when(reviewRepository.calculateAverageRating(productId, ReviewStatus.APPROVED)).thenReturn(4.8);
        when(reviewRepository.countByProductIdAndStatus(productId, ReviewStatus.APPROVED)).thenReturn(1L);
        when(reviewRepository.countByProductIdAndStatusAndRating(eq(productId), eq(ReviewStatus.APPROVED), anyInt())).thenReturn(1L);

        ProductReviewSummaryResponse response = reviewService.getProductReviews(productId, PageRequest.of(0, 10));

        assertThat(response).isNotNull();
        assertThat(response.averageRating()).isEqualTo(4.8);
        assertThat(response.totalReviews()).isEqualTo(1L);
        assertThat(response.ratingDistribution().get(5)).isEqualTo(1L);
    }
}
