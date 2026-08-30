package com.carmats.campaign.entity;

import com.carmats.common.entity.BaseEntity;
import com.carmats.order.entity.Order;
import com.carmats.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_usages")
public class CouponUsage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "guest_email", length = 150)
    private String guestEmail;

    @Column(name = "discount_applied", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountApplied;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt = LocalDateTime.now();

    protected CouponUsage() {
    }

    public CouponUsage(
            Coupon coupon,
            Order order,
            User user,
            String guestEmail,
            BigDecimal discountApplied
    ) {
        this.coupon = coupon;
        this.order = order;
        this.user = user;
        this.guestEmail = guestEmail;
        this.discountApplied = discountApplied;
        this.usedAt = LocalDateTime.now();
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public Order getOrder() {
        return order;
    }

    public User getUser() {
        return user;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public BigDecimal getDiscountApplied() {
        return discountApplied;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }
}
