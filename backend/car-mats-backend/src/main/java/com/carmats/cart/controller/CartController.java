package com.carmats.cart.controller;

import com.carmats.cart.dto.request.AddToCartRequest;
import com.carmats.cart.dto.request.MergeCartRequest;
import com.carmats.cart.dto.request.UpdateCartItemQuantityRequest;
import com.carmats.cart.dto.response.CartResponse;
import com.carmats.cart.service.CartService;
import com.carmats.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Sepet Yönetimi", description = "Misafir ve kayıtlı kullanıcı sepet işlemleri")
@RestController
@RequestMapping("/api/v1/cart")
@Validated
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Mevcut sepeti getirir", description = "Giriş yapmış kullanıcının veya misafir kullanıcının (X-Guest-Token ile) aktif sepetini döner.")
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(cartService.getCart(userId, guestToken));
    }

    @Operation(summary = "Sepete ürün ekler", description = "Sepete yeni ürün ekler veya mevcut ürünün adedini artırır.")
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @Valid @RequestBody AddToCartRequest request
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(cartService.addItem(userId, guestToken, request));
    }

    @Operation(summary = "Sepet kalemi adet günceller", description = "Belirtilen sepet kaleminin adedini günceller.")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, guestToken, itemId, request));
    }

    @Operation(summary = "Sepetten ürün çıkarır", description = "Belirtilen sepet kalemini sepetten kaldırır.")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PathVariable UUID itemId
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(cartService.removeItem(userId, guestToken, itemId));
    }

    @Operation(summary = "Sepeti temizler", description = "Sepetteki tüm ürünleri kaldırır.")
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        cartService.clearCart(userId, guestToken);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Misafir sepetini birleştirir", description = "Kullanıcı giriş yaptığında misafir sepetindeki ürünleri kullanıcı hesabına aktarır.")
    @PostMapping("/merge")
    public ResponseEntity<CartResponse> mergeCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MergeCartRequest request
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(cartService.mergeCart(userId, request.guestToken()));
    }
}
