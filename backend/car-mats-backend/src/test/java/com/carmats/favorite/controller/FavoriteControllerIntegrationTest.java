package com.carmats.favorite.controller;

import com.carmats.catalog.entity.Category;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.CategoryRepository;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.config.security.JwtService;
import com.carmats.favorite.repository.FavoriteRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FavoriteControllerIntegrationTest {

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
    private FavoriteRepository favoriteRepository;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private String userToken;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();

        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() ->
                categoryRepository.save(new Category("Paspas", "paspas-" + UUID.randomUUID()))
        );

        testProduct = new Product(
                category,
                "Fav Prod " + UUID.randomUUID().toString().substring(0, 6),
                "fav-prod-" + UUID.randomUUID(),
                "FAV-" + UUID.randomUUID().toString().substring(0, 6),
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

        String email = "fav.user." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        testUser = new User(email, "pass", "Fav", "Tester", null);
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(testUser::addRole);
        testUser = userRepository.save(testUser);
        userToken = jwtService.generateAccessToken(testUser.getId(), testUser.getEmail(), List.of("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("POST /api/v1/favorites/{productId} should toggle favorite on and off")
    void shouldToggleFavorite() throws Exception {
        // Toggle ON
        mockMvc.perform(post("/api/v1/favorites/" + testProduct.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFavorite", is(true)))
                .andExpect(jsonPath("$.productId", is(testProduct.getId().toString())));

        // Verify in list
        mockMvc.perform(get("/api/v1/favorites")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].productId", is(testProduct.getId().toString())));

        // Toggle OFF
        mockMvc.perform(post("/api/v1/favorites/" + testProduct.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFavorite", is(false)));
    }

    @Test
    @DisplayName("DELETE /api/v1/favorites/{productId} should remove favorite")
    void shouldRemoveFavorite() throws Exception {
        mockMvc.perform(post("/api/v1/favorites/" + testProduct.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/favorites/" + testProduct.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());
    }
}
