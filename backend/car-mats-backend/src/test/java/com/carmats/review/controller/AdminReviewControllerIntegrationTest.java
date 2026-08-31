package com.carmats.review.controller;

import com.carmats.catalog.entity.Category;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.CategoryRepository;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.config.security.JwtService;
import com.carmats.review.entity.ProductReview;
import com.carmats.review.entity.ReviewStatus;
import com.carmats.review.repository.ProductReviewRepository;
import com.carmats.user.entity.Role;
import com.carmats.user.entity.User;
import com.carmats.user.repository.RoleRepository;
import com.carmats.user.repository.UserRepository;
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminReviewControllerIntegrationTest {

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

    private User adminUser;
    private User customerUser;
    private String adminToken;
    private String customerToken;
    private Product testProduct;
    private ProductReview testReview;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() ->
                categoryRepository.save(new Category("Paspas", "paspas-" + UUID.randomUUID()))
        );

        testProduct = new Product(
                category,
                "Admin Review Prod " + UUID.randomUUID().toString().substring(0, 6),
                "admin-review-prod-" + UUID.randomUUID(),
                "AREV-" + UUID.randomUUID().toString().substring(0, 6),
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

        String adminEmail = "admin.rev." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        adminUser = new User(adminEmail, "pass", "Admin", "User", null);
        roleRepository.findByName(Role.ROLE_ADMIN).ifPresent(adminUser::addRole);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateAccessToken(adminUser.getId(), adminUser.getEmail(), List.of("ROLE_ADMIN"));

        String customerEmail = "cust.rev." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        customerUser = new User(customerEmail, "pass", "Customer", "User", null);
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(customerUser::addRole);
        customerUser = userRepository.save(customerUser);
        customerToken = jwtService.generateAccessToken(customerUser.getId(), customerUser.getEmail(), List.of("ROLE_CUSTOMER"));

        testReview = new ProductReview(
                testProduct,
                customerUser,
                null,
                4,
                "İyi ürün",
                "Kargo hızlı geldi, fena değil.",
                false,
                ReviewStatus.PENDING
        );
        testReview = reviewRepository.save(testReview);
    }

    @Test
    @DisplayName("GET /api/v1/admin/reviews should return list of reviews for ADMIN")
    void shouldListReviewsForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reviews")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/reviews/{id}/status should approve review for ADMIN")
    void shouldApproveReviewForAdmin() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/reviews/" + testReview.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"APPROVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/reviews should return 403 Forbidden for non-admin CUSTOMER")
    void shouldRejectCustomerForAdminReviews() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reviews")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }
}
