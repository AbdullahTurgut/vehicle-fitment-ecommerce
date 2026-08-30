package com.carmats.order.controller;

import com.carmats.common.response.PageResponse;
import com.carmats.config.security.CustomUserDetails;
import com.carmats.order.dto.request.UpdateOrderStatusRequest;
import com.carmats.order.dto.response.OrderResponse;
import com.carmats.order.dto.response.OrderSummaryResponse;
import com.carmats.order.entity.OrderStatus;
import com.carmats.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin Sipariş Yönetimi (Admin Order)", description = "Yönetici sipariş listeleme, detay ve durum güncelleme API'leri")
@RestController
@RequestMapping("/api/v1/admin/orders")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Tüm siparişleri listele", description = "Duruma göre filtreleyerek sayfalı tüm siparişleri listeler.")
    @GetMapping
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(status, pageable));
    }

    @Operation(summary = "Admin sipariş detayı", description = "Sipariş ID'sine göre ürünler, adresler ve durum geçmişiyle birlikte siparişi döner.")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getAdminOrderDetail(orderId));
    }

    @Operation(summary = "Sipariş durumunu güncelle", description = "Siparişin durumunu günceller ve tarihçeye not ekler. İptal durumunda stokları iade eder.")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        String changedBy = userDetails != null ? userDetails.getUsername() : "ADMIN";
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request, changedBy));
    }
}
