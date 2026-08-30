package com.carmats.order.controller;

import com.carmats.common.response.PageResponse;
import com.carmats.config.security.CustomUserDetails;
import com.carmats.order.dto.request.CreateOrderRequest;
import com.carmats.order.dto.response.OrderResponse;
import com.carmats.order.dto.response.OrderSummaryResponse;
import com.carmats.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Siparişler (Order)", description = "Sipariş oluşturma, sorgulama ve iptal API'leri")
@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Sipariş oluştur", description = "Sepetteki ürünlerden yeni bir sipariş oluşturur ve stokları düşer.")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @Valid @RequestBody(required = false) CreateOrderRequest request
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        OrderResponse response = orderService.createOrder(userId, guestToken, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Kullanıcının sipariş geçmişi", description = "Oturum açmış kullanıcının önceki siparişlerini sayfalı olarak listeler.")
    @GetMapping
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getUserOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getId(), pageable));
    }

    @Operation(summary = "Sipariş detayı", description = "Sipariş numarasına göre sipariş detayını, teslimat adresini ve ürünleri döner.")
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String orderNumber
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(orderService.getOrderByNumber(userId, orderNumber));
    }

    @Operation(summary = "Sipariş iptal et", description = "Kargoya verilmemiş siparişi iptal eder ve stokları iade eder.")
    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String orderNumber,
            @RequestBody(required = false) Map<String, String> body
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(orderService.cancelOrder(userId, orderNumber, reason));
    }
}
