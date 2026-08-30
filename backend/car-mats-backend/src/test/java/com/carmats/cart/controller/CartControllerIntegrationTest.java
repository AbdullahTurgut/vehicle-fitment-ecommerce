package com.carmats.cart.controller;

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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerIntegrationTest {

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
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
                "Cart Test Paspas " + UUID.randomUUID().toString().substring(0, 6),
                "cart-test-paspas-" + UUID.randomUUID(),
                "CART-" + UUID.randomUUID().toString().substring(0, 6),
                "Test açıklama",
                "Kısa açıklama",
                new BigDecimal("1000.00"),
                new BigDecimal("800.00"),
                20,
                ProductStatus.ACTIVE,
                true,
                "TPE",
                "Sahler"
        );
        testProduct = productRepository.save(testProduct);

        String email = "cart.user." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        testUser = new User(email, "pass", "Cart", "User", "+905551112233");
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(testUser::addRole);
        testUser = userRepository.save(testUser);

        userToken = jwtService.generateAccessToken(testUser.getId(), testUser.getEmail(), List.of("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("Guest user can add, update quantity, remove, and clear cart")
    void shouldPerformGuestCartFlow() throws Exception {
        String guestToken = "guest-" + UUID.randomUUID();

        // 1. Add item to cart
        String addJson = String.format("""
                {
                    "productId": "%s",
                    "quantity": 2
                }
                """, testProduct.getId());

        String addResponse = mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-Guest-Token", guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalQuantity", is(2)))
                .andExpect(jsonPath("$.subtotal", is(1600.00)))
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(addResponse);
        String itemId = jsonNode.get("items").get(0).get("id").asText();

        // 2. Update quantity
        String updateJson = """
                {
                    "quantity": 5
                }
                """;

        mockMvc.perform(put("/api/v1/cart/items/{itemId}", itemId)
                        .header("X-Guest-Token", guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity", is(5)))
                .andExpect(jsonPath("$.subtotal", is(4000.00)));

        // 3. Remove item
        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", itemId)
                        .header("X-Guest-Token", guestToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalQuantity", is(0)));

        // 4. Add again and clear cart
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-Guest-Token", guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));

        mockMvc.perform(delete("/api/v1/cart")
                        .header("X-Guest-Token", guestToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cart")
                        .header("X-Guest-Token", guestToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    @DisplayName("Should merge guest cart into user cart")
    void shouldMergeGuestCartIntoUserCart() throws Exception {
        String guestToken = "guest-merge-" + UUID.randomUUID();

        // Add item as guest
        String addJson = String.format("""
                {
                    "productId": "%s",
                    "quantity": 3
                }
                """, testProduct.getId());

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-Guest-Token", guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity", is(3)));

        // Merge into logged in user
        String mergeJson = String.format("""
                {
                    "guestToken": "%s"
                }
                """, guestToken);

        mockMvc.perform(post("/api/v1/cart/merge")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mergeJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(testUser.getId().toString())))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalQuantity", is(3)));
    }

    @Test
    @DisplayName("Should reject adding item exceeding available stock")
    void shouldRejectExceedingStock() throws Exception {
        String addJson = String.format("""
                {
                    "productId": "%s",
                    "quantity": 50
                }
                """, testProduct.getId());

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INSUFFICIENT_STOCK")));
    }
}
