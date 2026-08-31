package com.carmats.campaign.controller;

import com.carmats.campaign.dto.request.ValidateCouponRequest;
import com.carmats.campaign.entity.Coupon;
import com.carmats.campaign.entity.DiscountType;
import com.carmats.campaign.repository.CouponRepository;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CouponControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository couponRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        String code = "TEST" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        testCoupon = new Coupon(
                code,
                "Entegrasyon Test Kuponu",
                DiscountType.PERCENTAGE,
                new BigDecimal("10.00"),
                new BigDecimal("500.00"),
                new BigDecimal("200.00"),
                50,
                2,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(10),
                true
        );
        testCoupon = couponRepository.save(testCoupon);
    }

    @Test
    @DisplayName("POST /api/v1/coupons/validate should validate coupon and return discount amount")
    void shouldValidateCouponSuccessfully() throws Exception {
        ValidateCouponRequest request = new ValidateCouponRequest(
                testCoupon.getCode(),
                new BigDecimal("1000.00")
        );

        mockMvc.perform(post("/api/v1/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.code", is(testCoupon.getCode())))
                .andExpect(jsonPath("$.discountAmount", is(100.00)))
                .andExpect(jsonPath("$.finalAmount", is(900.00)));
    }

    @Test
    @DisplayName("POST /api/v1/coupons/validate should return valid=false for non-existent coupon")
    void shouldReturnInvalidForNonExistentCoupon() throws Exception {
        ValidateCouponRequest request = new ValidateCouponRequest(
                "GECE_YARISI_YOK_KOD",
                new BigDecimal("1000.00")
        );

        mockMvc.perform(post("/api/v1/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(false)))
                .andExpect(jsonPath("$.message", is("Kupon kodu bulunamadı.")));
    }
}
