package com.carmats.review.controller;

import com.carmats.catalog.entity.Category;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.CategoryRepository;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.config.security.JwtService;
import com.carmats.review.dto.request.CreateReviewRequest;
import com.carmats.review.entity.ProductReview;
import com.carmats.review.entity.ReviewStatus;
import com.carmats.review.repository.ProductReviewRepository;
import com.carmats.user.entity.Role;
import com.carmats.user.entity.User;
import com.carmats.user.repository.RoleRepository;
import com.carmats.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;
    private String userToken;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() ->
                categoryRepository.save(new Category("Paspas", "paspas-" + UUID.randomUUID()))
        );

        testProduct = new Product(
                category,
                "Review Prod " + UUID.randomUUID().toString().substring(0, 6),
                "review-prod-" + UUID.randomUUID(),
                "REV-" + UUID.randomUUID().toString().substring(0, 6),
                "Açıklama",
                "Kısa açıklama",
                new BigDecimal("1500.00"),
                new BigDecimal("1200.00"),
                20,
                ProductStatus.ACTIVE,
                true,
                "TPE",
                "Sahler"
        );
        testProduct = productRepository.save(testProduct);

        String email = "review.user." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        testUser = new User(email, "pass", "Reviewer", "User", null);
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(testUser::addRole);
        testUser = userRepository.save(testUser);
        userToken = jwtService.generateAccessToken(testUser.getId(), testUser.getEmail(), List.of("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("POST /api/v1/products/{productId}/reviews should create review for authenticated user")
    void shouldCreateReviewForAuthenticatedUser() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                5,
                "Harika bir ürün",
                "Aracıma tam uyum sağladı, malzeme kalitesi mükemmel."
        );

        mockMvc.perform(post("/api/v1/products/" + testProduct.getId() + "/reviews")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating", is(5)))
                .andExpect(jsonPath("$.title", is("Harika bir ürün")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId}/reviews should return approved reviews and rating stats")
    void shouldReturnApprovedReviews() throws Exception {
        ProductReview approvedReview = new ProductReview(
                testProduct,
                testUser,
                null,
                5,
                "Onaylı Yorum",
                "Süper paspas, çok beğendim.",
                false,
                ReviewStatus.APPROVED
        );
        reviewRepository.save(approvedReview);

        mockMvc.perform(get("/api/v1/products/" + testProduct.getId() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating", is(5.0)))
                .andExpect(jsonPath("$.totalReviews", is(1)))
                .andExpect(jsonPath("$.reviews.content", hasSize(1)));
    }
}
