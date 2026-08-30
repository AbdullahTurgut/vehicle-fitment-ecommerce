package com.carmats.campaign.controller;

import com.carmats.campaign.dto.request.CreateCouponRequest;
import com.carmats.campaign.entity.Coupon;
import com.carmats.campaign.entity.DiscountType;
import com.carmats.campaign.repository.CouponRepository;
import com.carmats.config.security.JwtService;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCouponControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User adminUser;
    private User customerUser;
    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        String adminEmail = "admin.coupon." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        adminUser = new User(adminEmail, "pass", "Admin", "User", null);
        roleRepository.findByName(Role.ROLE_ADMIN).ifPresent(adminUser::addRole);
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateAccessToken(adminUser.getId(), adminUser.getEmail(), List.of("ROLE_ADMIN"));

        String customerEmail = "cust.coupon." + UUID.randomUUID().toString().substring(0, 8) + "@carmats.local";
        customerUser = new User(customerEmail, "pass", "Customer", "User", null);
        roleRepository.findByName(Role.ROLE_CUSTOMER).ifPresent(customerUser::addRole);
        customerUser = userRepository.save(customerUser);
        customerToken = jwtService.generateAccessToken(customerUser.getId(), customerUser.getEmail(), List.of("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/coupons should create coupon for ADMIN")
    void shouldCreateCouponForAdmin() throws Exception {
        String code = "ADMIN" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        CreateCouponRequest request = new CreateCouponRequest(
                code,
                "Admin Tarafından Oluşturulan Kupon",
                DiscountType.PERCENTAGE,
                new BigDecimal("25.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                100,
                1,
                null,
                null,
                true
        );

        mockMvc.perform(post("/api/v1/admin/coupons")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(code)))
                .andExpect(jsonPath("$.discountValue", is(25.00)));
    }

    @Test
    @DisplayName("POST /api/v1/admin/coupons should return 403 Forbidden for non-admin CUSTOMER")
    void shouldRejectCustomerForAdminCouponCreation() throws Exception {
        CreateCouponRequest request = new CreateCouponRequest(
                "HACK20",
                "Deneme",
                DiscountType.FIXED_AMOUNT,
                new BigDecimal("50.00"),
                null, null, null, 1, null, null, true
        );

        mockMvc.perform(post("/api/v1/admin/coupons")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/admin/coupons should return list of coupons for ADMIN")
    void shouldListCouponsForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/coupons")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/coupons/{id}/status should toggle coupon status")
    void shouldUpdateCouponStatus() throws Exception {
        String code = "STATUSTEST" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Coupon coupon = new Coupon(
                code, "Açıklama", DiscountType.FIXED_AMOUNT, new BigDecimal("50.00"),
                null, null, null, 1, null, null, true
        );
        coupon = couponRepository.save(coupon);

        mockMvc.perform(patch("/api/v1/admin/coupons/" + coupon.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));
    }
}
