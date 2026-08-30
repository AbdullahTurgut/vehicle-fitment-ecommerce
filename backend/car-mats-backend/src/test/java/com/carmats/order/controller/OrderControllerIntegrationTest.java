package com.carmats.order.controller;

import com.carmats.cart.dto.request.AddToCartRequest;
import com.carmats.cart.service.CartService;
import com.carmats.catalog.entity.Category;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.CategoryRepository;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.config.security.JwtService;
import com.carmats.order.dto.request.CreateOrderRequest;
import com.carmats.order.dto.request.CustomOrderAddressDto;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

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
                "Order Integration Product " + UUID.randomUUID().toString().substring(0, 6),
                "order-product-" + UUID.randomUUID(),
                "ORDP-" + UUID.randomUUID().toString().substring(0, 6),
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

        String email = "order.user." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        testUser = new User(email, "pass", "Order", "Tester", "+905551112233");
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(testUser::addRole);
        testUser = userRepository.save(testUser);

        userToken = jwtService.generateAccessToken(testUser.getId(), testUser.getEmail(), List.of("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("Should perform full customer order lifecycle: create, list, get detail, and cancel")
    void shouldPerformFullOrderLifecycle() throws Exception {
        // 1. Add item to cart
        cartService.addItem(testUser.getId(), null, new AddToCartRequest(testProduct.getId(), null, 2));

        CustomOrderAddressDto customAddress = new CustomOrderAddressDto(
                "Order", "Tester", "+905551112233", "İstanbul", "Kadıköy",
                "Moda", "Caferağa Mah. No:5 D:2", "34710", null, null, null
        );

        CreateOrderRequest createRequest = new CreateOrderRequest(
                null, null, customAddress, null, null, null, null, null, "Sipariş notu", null
        );

        // 2. Create order (POST /api/v1/orders)
        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber", startsWith("ORD-")))
                .andExpect(jsonPath("$.status", is("PENDING_PAYMENT")))
                .andExpect(jsonPath("$.subtotal", is(2400.00)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.deliveryAddress.city", is("İstanbul")))
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        String orderNumber = objectMapper.readTree(responseContent).get("orderNumber").asText();

        // 3. List user orders (GET /api/v1/orders)
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].orderNumber", is(orderNumber)));

        // 4. Get order detail (GET /api/v1/orders/{orderNumber})
        mockMvc.perform(get("/api/v1/orders/" + orderNumber)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber", is(orderNumber)))
                .andExpect(jsonPath("$.status", is("PENDING_PAYMENT")));

        // 5. Cancel order (POST /api/v1/orders/{orderNumber}/cancel)
        mockMvc.perform(post("/api/v1/orders/" + orderNumber + "/cancel")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Test iptali\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }
}
