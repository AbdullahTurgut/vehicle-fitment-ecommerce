package com.carmats.cart.mapper;

import com.carmats.cart.dto.response.CartItemResponse;
import com.carmats.cart.dto.response.CartResponse;
import com.carmats.cart.entity.Cart;
import com.carmats.cart.entity.CartItem;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CartMapper {

    private CartMapper() {
    }

    public static CartItemResponse toCartItemResponse(CartItem item, String primaryImageUrl) {
        Product product = item.getProduct();

        String variantName = item.getVehicleVariant() != null
                ? item.getVehicleVariant().getName()
                : null;

        boolean available = product.getStatus() == ProductStatus.ACTIVE
                && product.getStockQuantity() >= item.getQuantity();

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                primaryImageUrl,
                item.getVehicleVariant() != null ? item.getVehicleVariant().getId() : null,
                variantName,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal(),
                product.getStockQuantity(),
                available
        );
    }

    public static CartResponse toCartResponse(Cart cart, Map<UUID, String> primaryImageMap) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> toCartItemResponse(
                        item,
                        primaryImageMap != null ? primaryImageMap.get(item.getProduct().getId()) : null
                ))
                .toList();

        int totalQuantity = itemResponses.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        BigDecimal subtotal = itemResponses.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.subtract(discount);

        return new CartResponse(
                cart.getId(),
                cart.getUser() != null ? cart.getUser().getId() : null,
                cart.getGuestToken(),
                itemResponses,
                totalQuantity,
                subtotal,
                discount,
                totalAmount
        );
    }
}
