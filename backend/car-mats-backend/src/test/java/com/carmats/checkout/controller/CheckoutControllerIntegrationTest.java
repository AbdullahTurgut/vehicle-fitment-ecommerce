package com.carmats.checkout.controller;

import com.carmats.cart.dto.request.AddToCartRequest;
import com.carmats.cart.service.CartService;
import com.carmats.catalog.entity.Category;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.CategoryRepository;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.config.security.JwtService;
import com.carmats.user.entity.Role;
import com.carmats.user.entity.User;
import com.carmats.user.repository.RoleRepository;
import com.carmats.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CheckoutControllerIntegrationTest {

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
    private CartService cartService;

    @Autowired
    private JwtService jwtService;

    private Product testProduct;
    private User testUser;
    private String userToken;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() ->
                categoryRepository.save(new Category("Paspas", "paspas-" + UUID.randomUUID()))
        );

        testProduct = new Product(
                category,
                "Checkout Test Product " + UUID.randomUUID().toString().substring(0, 6),
                "checkout-product-" + UUID.randomUUID(),
                "CHK-" + UUID.randomUUID().toString().substring(0, 6),
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

        String email = "checkout.user." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        testUser = new User(email, "pass", "Checkout", "User", "+905551112233");
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(testUser::addRole);
        testUser = userRepository.save(testUser);

        userToken = jwtService.generateAccessToken(testUser.getId(), testUser.getEmail(), List.of("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("POST /api/v1/checkout/preview returns checkout summary")
    void shouldReturnCheckoutSummary() throws Exception {
        // Add item to cart
        cartService.addItem(testUser.getId(), null, new AddToCartRequest(testProduct.getId(), null, 1));

        mockMvc.perform(post("/api/v1/checkout/preview")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.subtotal", is(1200.00)))
                .andExpect(jsonPath("$.freeShippingApplied", is(true)))
                .andExpect(jsonPath("$.shippingFee").value(0))
                .andExpect(jsonPath("$.grandTotal", is(1200.00)));
    }

    @Test
    @DisplayName("POST /api/v1/checkout/preview fails on empty cart")
    void shouldFailOnEmptyCart() throws Exception {
        mockMvc.perform(post("/api/v1/checkout/preview")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("EMPTY_CART")));
    }

    @Test
    @DisplayName("POST /api/v1/checkout/validate returns valid true when cart is valid")
    void shouldValidateCheckout() throws Exception {
        cartService.addItem(testUser.getId(), null, new AddToCartRequest(testProduct.getId(), null, 2));

        mockMvc.perform(post("/api/v1/checkout/validate")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.errors", empty()));
    }
}
