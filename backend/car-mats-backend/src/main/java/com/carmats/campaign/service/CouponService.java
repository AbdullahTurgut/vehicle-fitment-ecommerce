package com.carmats.campaign.service;

import com.carmats.campaign.dto.request.CreateCouponRequest;
import com.carmats.campaign.dto.request.UpdateCouponRequest;
import com.carmats.campaign.dto.request.ValidateCouponRequest;
import com.carmats.campaign.dto.response.CouponResponse;
import com.carmats.campaign.dto.response.CouponValidationResponse;
import com.carmats.campaign.entity.Coupon;
import com.carmats.campaign.entity.CouponUsage;
import com.carmats.campaign.mapper.CouponMapper;
import com.carmats.campaign.repository.CouponRepository;
import com.carmats.campaign.repository.CouponUsageRepository;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.common.response.PageResponse;
import com.carmats.order.entity.Order;
import com.carmats.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    public CouponService(
            CouponRepository couponRepository,
            CouponUsageRepository couponUsageRepository
    ) {
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
    }

    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(UUID userId, String guestEmail, ValidateCouponRequest request) {
        Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCase(request.code());
        if (couponOpt.isEmpty()) {
            return CouponValidationResponse.invalid("Kupon kodu bulunamadı.");
        }

        Coupon coupon = couponOpt.get();

        if (!coupon.isCurrentlyActive()) {
            return CouponValidationResponse.invalid("Bu kupon aktif değil veya kullanım süresi dolmuş.");
        }

        if (coupon.getMinimumOrderAmount() != null &&
                request.cartSubtotal().compareTo(coupon.getMinimumOrderAmount()) < 0) {
            return CouponValidationResponse.invalid(
                    String.format("Bu kupon minimum %s TL sepet tutarında geçerlidir.", coupon.getMinimumOrderAmount())
            );
        }

        if (userId != null) {
            long used = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
            if (used >= coupon.getUsageLimitPerUser()) {
                return CouponValidationResponse.invalid("Bu kupon için kullanım limitinize ulaştınız.");
            }
        } else if (guestEmail != null && !guestEmail.isBlank()) {
            long used = couponUsageRepository.countByCouponIdAndGuestEmailIgnoreCase(coupon.getId(), guestEmail);
            if (used >= coupon.getUsageLimitPerUser()) {
                return CouponValidationResponse.invalid("Bu kupon için kullanım limitinize ulaştınız.");
            }
        }

        BigDecimal discountAmount = coupon.calculateDiscount(request.cartSubtotal());
        BigDecimal finalAmount = request.cartSubtotal().subtract(discountAmount).max(BigDecimal.ZERO);

        return CouponValidationResponse.valid(
                coupon.getCode(),
                discountAmount,
                finalAmount,
                CouponMapper.toResponse(coupon)
        );
    }

    public CouponUsage recordCouponUsage(
            String couponCode,
            Order order,
            BigDecimal discountApplied,
            User user,
            String guestEmail
    ) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(couponCode)
                .orElseThrow(() -> new NotFoundException("COUPON_NOT_FOUND", "Kupon bulunamadı."));

        coupon.incrementUsedCount();
        couponRepository.save(coupon);

        CouponUsage usage = new CouponUsage(coupon, order, user, guestEmail, discountApplied);
        return couponUsageRepository.save(usage);
    }

    // Admin APIs
    public CouponResponse createCoupon(CreateCouponRequest request) {
        String cleanCode = request.code().toUpperCase().trim();
        if (couponRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new BusinessException("COUPON_CODE_EXISTS", "Bu kupon kodu zaten mevcuttur.");
        }

        Coupon coupon = new Coupon(
                cleanCode,
                request.description(),
                request.discountType(),
                request.discountValue(),
                request.minimumOrderAmount(),
                request.maxDiscountAmount(),
                request.usageLimit(),
                request.usageLimitPerUser() != null ? request.usageLimitPerUser() : 1,
                request.startDate(),
                request.endDate(),
                request.active() != null ? request.active() : true
        );

        Coupon saved = couponRepository.save(coupon);
        return CouponMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponResponse> getAllCoupons(Boolean active, Pageable pageable) {
        Page<Coupon> page = couponRepository.findAllByActiveFilter(active, pageable);
        return PageResponse.from(page.map(CouponMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CouponResponse getCouponById(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("COUPON_NOT_FOUND", "Kupon bulunamadı."));
        return CouponMapper.toResponse(coupon);
    }

    public CouponResponse updateCoupon(UUID id, UpdateCouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("COUPON_NOT_FOUND", "Kupon bulunamadı."));

        coupon.setDescription(request.description());
        coupon.setDiscountType(request.discountType());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinimumOrderAmount(request.minimumOrderAmount());
        coupon.setMaxDiscountAmount(request.maxDiscountAmount());
        coupon.setUsageLimit(request.usageLimit());
        if (request.usageLimitPerUser() != null) {
            coupon.setUsageLimitPerUser(request.usageLimitPerUser());
        }
        coupon.setStartDate(request.startDate());
        coupon.setEndDate(request.endDate());
        if (request.active() != null) {
            coupon.setActive(request.active());
        }

        Coupon saved = couponRepository.save(coupon);
        return CouponMapper.toResponse(saved);
    }

    public CouponResponse updateCouponStatus(UUID id, boolean active) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("COUPON_NOT_FOUND", "Kupon bulunamadı."));
        coupon.setActive(active);
        Coupon saved = couponRepository.save(coupon);
        return CouponMapper.toResponse(saved);
    }
}
