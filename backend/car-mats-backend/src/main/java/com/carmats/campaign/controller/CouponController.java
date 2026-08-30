package com.carmats.campaign.controller;

import com.carmats.campaign.dto.request.ValidateCouponRequest;
import com.carmats.campaign.dto.response.CouponValidationResponse;
import com.carmats.campaign.service.CouponService;
import com.carmats.config.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String guestEmail,
            @Valid @RequestBody ValidateCouponRequest request
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        CouponValidationResponse response = couponService.validateCoupon(userId, guestEmail, request);
        return ResponseEntity.ok(response);
    }
}
