package com.carmats.campaign.repository;

import com.carmats.campaign.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {

    long countByCouponIdAndUserId(UUID couponId, UUID userId);

    long countByCouponIdAndGuestEmailIgnoreCase(UUID couponId, String guestEmail);

    List<CouponUsage> findAllByCouponIdOrderByUsedAtDesc(UUID couponId);

    List<CouponUsage> findAllByOrderId(UUID orderId);
}
