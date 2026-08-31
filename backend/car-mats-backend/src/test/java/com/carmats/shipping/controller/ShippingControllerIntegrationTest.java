package com.carmats.shipping.controller;

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
import com.carmats.shipping.dto.request.CreateShipmentRequest;
import com.carmats.shipping.dto.response.ShipmentResponse;
import com.carmats.shipping.entity.ShippingCarrier;
import com.carmats.shipping.service.ShippingService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ShippingControllerIntegrationTest {

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
    private ShippingService shippingService;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private String userToken;
    private OrderResponse testOrder;
    private ShipmentResponse testShipment;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.findAll().stream().findFirst().orElseGet(() ->
                categoryRepository.save(new Category("Paspas", "paspas-" + UUID.randomUUID()))
        );

        Product product = new Product(
                category,
                "Shipping Prod " + UUID.randomUUID().toString().substring(0, 6),
                "shipping-prod-" + UUID.randomUUID(),
                "SHP-" + UUID.randomUUID().toString().substring(0, 6),
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

        String email = "ship.user." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        testUser = new User(email, "pass", "Shipping", "Tester", "+905551112233");
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(testUser::addRole);
        testUser = userRepository.save(testUser);
        userToken = jwtService.generateAccessToken(testUser.getId(), testUser.getEmail(), List.of("ROLE_CUSTOMER"));

        cartService.addItem(testUser.getId(), null, new AddToCartRequest(product.getId(), null, 1));
        CustomOrderAddressDto customAddress = new CustomOrderAddressDto(
                "Shipping", "Tester", "+905551112233", "Bursa", "Nilüfer",
                "Görükle", "Atatürk Cad. No:12", "16285", null, null, null
        );
        testOrder = orderService.createOrder(testUser.getId(), null, new CreateOrderRequest(
                null, null, customAddress, null, null, null, null, null, null, null
        ));

        testShipment = shippingService.createShipment(
                new CreateShipmentRequest(testOrder.id(), ShippingCarrier.YURTICI, null, null),
                "admin@carmats.local"
        );
    }

    @Test
    @DisplayName("GET /api/v1/shipments/orders/{orderNumber} should return shipment details for customer order")
    void shouldReturnShipmentForOrder() throws Exception {
        mockMvc.perform(get("/api/v1/shipments/orders/" + testOrder.orderNumber())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber", is(testOrder.orderNumber())))
                .andExpect(jsonPath("$.carrier", is("YURTICI")))
                .andExpect(jsonPath("$.trackingNumber", is(testShipment.trackingNumber())))
                .andExpect(jsonPath("$.trackingEvents", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/v1/shipments/track/{trackingNumber} should return public tracking info without authentication")
    void shouldReturnPublicTrackingInfo() throws Exception {
        mockMvc.perform(get("/api/v1/shipments/track/" + testShipment.trackingNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber", is(testShipment.trackingNumber())))
                .andExpect(jsonPath("$.carrier", is("YURTICI")))
                .andExpect(jsonPath("$.deliveryCity", is("Bursa")));
    }
}
