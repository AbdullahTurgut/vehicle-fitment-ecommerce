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
import com.carmats.order.dto.request.UpdateOrderStatusRequest;
import com.carmats.order.dto.response.OrderResponse;
import com.carmats.order.entity.OrderStatus;
import com.carmats.order.service.OrderService;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminOrderControllerIntegrationTest {

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
    private OrderService orderService;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User adminUser;
    private User customerUser;
    private String adminToken;
    private String customerToken;
    private OrderResponse testOrder;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() ->
                categoryRepository.save(new Category("Paspas", "paspas-" + UUID.randomUUID()))
        );

        Product product = new Product(
                category,
                "Admin Order Test " + UUID.randomUUID().toString().substring(0, 6),
                "admin-order-prod-" + UUID.randomUUID(),
                "AOP-" + UUID.randomUUID().toString().substring(0, 6),
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
        product = productRepository.save(product);

        String adminEmail = "admin.order." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        adminUser = new User(adminEmail, "pass", "Admin", "User", null);
        roleRepository.findByName(Role.ROLE_ADMIN).ifPresent(adminUser::addRole);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateAccessToken(adminUser.getId(), adminUser.getEmail(), List.of("ROLE_ADMIN"));

        String customerEmail = "customer.order." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        customerUser = new User(customerEmail, "pass", "Customer", "User", null);
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(customerUser::addRole);
        customerUser = userRepository.save(customerUser);
        customerToken = jwtService.generateAccessToken(customerUser.getId(), customerUser.getEmail(), List.of("ROLE_CUSTOMER"));

        // Create an order for testing
        cartService.addItem(customerUser.getId(), null, new AddToCartRequest(product.getId(), null, 1));
        CustomOrderAddressDto customAddress = new CustomOrderAddressDto(
                "Customer", "User", "+905551112233", "Ankara", "Çankaya",
                "Kızılay", "Atatürk Bulvarı No:1", "06420", null, null, null
        );
        testOrder = orderService.createOrder(customerUser.getId(), null, new CreateOrderRequest(
                null, null, customAddress, null, null, null, null, null, null, null
        ));
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders should list all orders for admin")
    void shouldListOrdersForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders should reject customer with 403 Forbidden")
    void shouldRejectCustomerForAdminOrders() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderId}/status should update status and record history")
    void shouldUpdateOrderStatus() throws Exception {
        UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest(
                OrderStatus.SHIPPED, "Kargo takip no: TR123456789"
        );

        mockMvc.perform(patch("/api/v1/admin/orders/" + testOrder.id() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHIPPED")))
                .andExpect(jsonPath("$.statusHistory", hasSize(greaterThanOrEqualTo(2))));
    }
}
