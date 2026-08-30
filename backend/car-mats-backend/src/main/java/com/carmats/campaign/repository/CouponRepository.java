package com.carmats.campaign.repository;

import com.carmats.campaign.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("SELECT c FROM Coupon c WHERE (:active IS NULL OR c.active = :active) ORDER BY c.createdAt DESC")
    Page<Coupon> findAllByActiveFilter(@Param("active") Boolean active, Pageable pageable);
}
