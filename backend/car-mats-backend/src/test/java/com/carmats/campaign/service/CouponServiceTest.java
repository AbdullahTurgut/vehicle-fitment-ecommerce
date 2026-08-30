package com.carmats.campaign.service;

import com.carmats.campaign.dto.request.CreateCouponRequest;
import com.carmats.campaign.dto.request.ValidateCouponRequest;
import com.carmats.campaign.dto.response.CouponResponse;
import com.carmats.campaign.dto.response.CouponValidationResponse;
import com.carmats.campaign.entity.Coupon;
import com.carmats.campaign.entity.DiscountType;
import com.carmats.campaign.repository.CouponRepository;
import com.carmats.campaign.repository.CouponUsageRepository;
import com.carmats.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @InjectMocks
    private CouponService couponService;

    private Coupon percentageCoupon;
    private Coupon fixedCoupon;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        percentageCoupon = new Coupon(
                "YAZ15",
                "%15 Yaz İndirimi",
                DiscountType.PERCENTAGE,
                new BigDecimal("15.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("300.00"),
                100,
                1,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                true
        );
        ReflectionTestUtils.setField(percentageCoupon, "id", UUID.randomUUID());

        fixedCoupon = new Coupon(
                "HOSGELDIN100",
                "100 TL Hoş Geldin İndirimi",
                DiscountType.FIXED_AMOUNT,
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                null,
                null,
                1,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                true
        );
        ReflectionTestUtils.setField(fixedCoupon, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("Should validate percentage coupon and calculate correct discount amount")
    void shouldValidatePercentageCouponSuccessfully() {
        ValidateCouponRequest request = new ValidateCouponRequest("YAZ15", new BigDecimal("1500.00"));

        when(couponRepository.findByCodeIgnoreCase("YAZ15")).thenReturn(Optional.of(percentageCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(percentageCoupon.getId(), userId)).thenReturn(0L);

        CouponValidationResponse response = couponService.validateCoupon(userId, null, request);

        assertThat(response.valid()).isTrue();
        // 1500 * 0.15 = 225.00
        assertThat(response.discountAmount()).isEqualByComparingTo(new BigDecimal("225.00"));
        assertThat(response.finalAmount()).isEqualByComparingTo(new BigDecimal("1275.00"));
    }

    @Test
    @DisplayName("Should apply max discount cap on percentage coupon when exceeded")
    void shouldApplyMaxDiscountCap() {
        // 3000 * 0.15 = 450.00, capped at maxDiscountAmount = 300.00
        ValidateCouponRequest request = new ValidateCouponRequest("YAZ15", new BigDecimal("3000.00"));

        when(couponRepository.findByCodeIgnoreCase("YAZ15")).thenReturn(Optional.of(percentageCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(percentageCoupon.getId(), userId)).thenReturn(0L);

        CouponValidationResponse response = couponService.validateCoupon(userId, null, request);

        assertThat(response.valid()).isTrue();
        assertThat(response.discountAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(response.finalAmount()).isEqualByComparingTo(new BigDecimal("2700.00"));
    }

    @Test
    @DisplayName("Should validate fixed amount coupon successfully")
    void shouldValidateFixedAmountCouponSuccessfully() {
        ValidateCouponRequest request = new ValidateCouponRequest("HOSGELDIN100", new BigDecimal("800.00"));

        when(couponRepository.findByCodeIgnoreCase("HOSGELDIN100")).thenReturn(Optional.of(fixedCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(fixedCoupon.getId(), userId)).thenReturn(0L);

        CouponValidationResponse response = couponService.validateCoupon(userId, null, request);

        assertThat(response.valid()).isTrue();
        assertThat(response.discountAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.finalAmount()).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    @DisplayName("Should reject coupon when minimum order amount is not met")
    void shouldRejectCouponWhenMinimumAmountNotMet() {
        ValidateCouponRequest request = new ValidateCouponRequest("YAZ15", new BigDecimal("800.00")); // min is 1000

        when(couponRepository.findByCodeIgnoreCase("YAZ15")).thenReturn(Optional.of(percentageCoupon));

        CouponValidationResponse response = couponService.validateCoupon(userId, null, request);

        assertThat(response.valid()).isFalse();
        assertThat(response.message()).contains("minimum 1000.00 TL");
    }

    @Test
    @DisplayName("Should reject coupon when user reached usage limit")
    void shouldRejectCouponWhenUserLimitReached() {
        ValidateCouponRequest request = new ValidateCouponRequest("YAZ15", new BigDecimal("1500.00"));

        when(couponRepository.findByCodeIgnoreCase("YAZ15")).thenReturn(Optional.of(percentageCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(percentageCoupon.getId(), userId)).thenReturn(1L); // limit is 1

        CouponValidationResponse response = couponService.validateCoupon(userId, null, request);

        assertThat(response.valid()).isFalse();
        assertThat(response.message()).contains("kullanım limitinize ulaştınız");
    }

    @Test
    @DisplayName("Should create coupon successfully")
    void shouldCreateCouponSuccessfully() {
        CreateCouponRequest request = new CreateCouponRequest(
                "YENIYIL20",
                "Yeni Yıl İndirimi",
                DiscountType.PERCENTAGE,
                new BigDecimal("20.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                200,
                1,
                LocalDateTime.now(),
                LocalDateTime.now().plusMonths(1),
                true
        );

        when(couponRepository.existsByCodeIgnoreCase("YENIYIL20")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> {
            Coupon c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", UUID.randomUUID());
            return c;
        });

        CouponResponse response = couponService.createCoupon(request);

        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("YENIYIL20");
        assertThat(response.discountValue()).isEqualByComparingTo(new BigDecimal("20.00"));
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when creating duplicate coupon code")
    void shouldThrowExceptionWhenDuplicateCode() {
        CreateCouponRequest request = new CreateCouponRequest(
                "YAZ15",
                "Tekrar",
                DiscountType.PERCENTAGE,
                new BigDecimal("15.00"),
                null, null, null, 1, null, null, true
        );

        when(couponRepository.existsByCodeIgnoreCase("YAZ15")).thenReturn(true);

        assertThatThrownBy(() -> couponService.createCoupon(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten mevcuttur");

        verify(couponRepository, never()).save(any());
    }
}
