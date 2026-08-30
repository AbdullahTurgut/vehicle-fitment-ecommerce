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
import com.carmats.shipping.dto.request.UpdateShipmentStatusRequest;
import com.carmats.shipping.dto.response.ShipmentResponse;
import com.carmats.shipping.entity.ShipmentStatus;
import com.carmats.shipping.entity.ShippingCarrier;
import com.carmats.shipping.service.ShippingService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminShippingControllerIntegrationTest {

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
                "Admin Shipping Prod " + UUID.randomUUID().toString().substring(0, 6),
                "admin-ship-prod-" + UUID.randomUUID(),
                "ASH-" + UUID.randomUUID().toString().substring(0, 6),
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

        String adminEmail = "admin.ship." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        adminUser = new User(adminEmail, "pass", "Admin", "User", null);
        roleRepository.findByName(Role.ROLE_ADMIN).ifPresent(adminUser::addRole);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateAccessToken(adminUser.getId(), adminUser.getEmail(), List.of("ROLE_ADMIN"));

        String customerEmail = "cust.ship." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        customerUser = new User(customerEmail, "pass", "Customer", "User", null);
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(customerUser::addRole);
        customerUser = userRepository.save(customerUser);
        customerToken = jwtService.generateAccessToken(customerUser.getId(), customerUser.getEmail(), List.of("ROLE_CUSTOMER"));

        cartService.addItem(customerUser.getId(), null, new AddToCartRequest(product.getId(), null, 1));
        CustomOrderAddressDto customAddress = new CustomOrderAddressDto(
                "Customer", "User", "+905551112233", "Antalya", "Muratpaşa",
                "Lara", "Şirinyalı Mah. No:8", "07160", null, null, null
        );
        testOrder = orderService.createOrder(customerUser.getId(), null, new CreateOrderRequest(
                null, null, customAddress, null, null, null, null, null, null, null
        ));
    }

    @Test
    @DisplayName("POST /api/v1/admin/shipments should create shipment when requested by ADMIN")
    void shouldCreateShipmentForAdmin() throws Exception {
        CreateShipmentRequest request = new CreateShipmentRequest(
                testOrder.id(), ShippingCarrier.ARAS, null, null
        );

        mockMvc.perform(post("/api/v1/admin/shipments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carrier", is("ARAS")))
                .andExpect(jsonPath("$.status", is("CREATED")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/shipments should return 403 Forbidden for non-admin customer")
    void shouldRejectCustomerForAdminShipmentCreation() throws Exception {
        CreateShipmentRequest request = new CreateShipmentRequest(
                testOrder.id(), ShippingCarrier.ARAS, null, null
        );

        mockMvc.perform(post("/api/v1/admin/shipments")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/shipments/{shipmentId}/status should update shipment status and record event")
    void shouldUpdateShipmentStatus() throws Exception {
        ShipmentResponse shipment = shippingService.createShipment(
                new CreateShipmentRequest(testOrder.id(), ShippingCarrier.MNG, null, null),
                "admin@carmats.local"
        );

        UpdateShipmentStatusRequest updateRequest = new UpdateShipmentStatusRequest(
                ShipmentStatus.DELIVERED, "Muratpaşa Dağıtım", "Müşteriye bizzat teslim edildi."
        );

        mockMvc.perform(patch("/api/v1/admin/shipments/" + shipment.id() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DELIVERED")));
    }
}
