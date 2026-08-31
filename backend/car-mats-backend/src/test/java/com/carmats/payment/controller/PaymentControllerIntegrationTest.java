package com.carmats.payment.controller;

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
import com.carmats.order.dto.response.OrderResponse;
import com.carmats.order.service.OrderService;
import com.carmats.payment.dto.request.ProcessPaymentRequest;
import com.carmats.payment.entity.PaymentMethod;
import com.carmats.payment.repository.PaymentRepository;
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentControllerIntegrationTest {

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
    private PaymentRepository paymentRepository;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;
    private String userToken;
    private OrderResponse testOrder;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() ->
                categoryRepository.save(new Category("Paspas", "paspas-" + UUID.randomUUID()))
        );

        Product product = new Product(
                category,
                "Payment Product " + UUID.randomUUID().toString().substring(0, 6),
                "payment-prod-" + UUID.randomUUID(),
                "PAYP-" + UUID.randomUUID().toString().substring(0, 6),
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

        String email = "pay.user." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        testUser = new User(email, "pass", "Payment", "Tester", "+905551112233");
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(testUser::addRole);
        testUser = userRepository.save(testUser);
        userToken = jwtService.generateAccessToken(testUser.getId(), testUser.getEmail(), List.of("ROLE_CUSTOMER"));

        cartService.addItem(testUser.getId(), null, new AddToCartRequest(product.getId(), null, 1));
        CustomOrderAddressDto customAddress = new CustomOrderAddressDto(
                "Payment", "Tester", "+905551112233", "İzmir", "Konak",
                "Alsancak", "Kıbrıs Şehitleri Cad. No:10", "35220", null, null, null
        );
        testOrder = orderService.createOrder(testUser.getId(), null, new CreateOrderRequest(
                null, null, customAddress, null, null, null, null, null, null, null
        ));
    }

    @Test
    @DisplayName("POST /api/v1/payments/process should process payment successfully and update order to PAID")
    void shouldProcessPaymentSuccessfully() throws Exception {
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                testOrder.orderNumber(),
                PaymentMethod.CREDIT_CARD,
                "Payment Tester",
                "5528790000000001",
                "12",
                "2028",
                "123",
                1
        );

        mockMvc.perform(post("/api/v1/payments/process")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.cardLastFour", is("0001")))
                .andExpect(jsonPath("$.paidAt", notNullValue()));

        // Check GET /api/v1/payments/orders/{orderNumber}
        mockMvc.perform(get("/api/v1/payments/orders/" + testOrder.orderNumber())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.orderNumber", is(testOrder.orderNumber())));
    }

    @Test
    @DisplayName("POST /api/v1/payments/process should reject declined card")
    void shouldRejectDeclinedCard() throws Exception {
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                testOrder.orderNumber(),
                PaymentMethod.CREDIT_CARD,
                "Payment Tester",
                "5528790000000002",
                "12",
                "2028",
                "123",
                1
        );

        mockMvc.perform(post("/api/v1/payments/process")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("CARD_DECLINED")));
    }
}
