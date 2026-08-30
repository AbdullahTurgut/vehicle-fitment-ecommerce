package com.carmats.order.mapper;

import com.carmats.order.dto.response.*;
import com.carmats.order.entity.*;

import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),
                item.getProductSlug(),
                item.getProductSku(),
                item.getPrimaryImageUrl(),
                item.getVehicleVariant() != null ? item.getVehicleVariant().getId() : null,
                item.getVehicleVariantName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    public static OrderAddressResponse toOrderAddressResponse(OrderAddress address) {
        if (address == null) {
            return null;
        }
        return new OrderAddressResponse(
                address.getId(),
                address.getAddressType(),
                address.getFirstName(),
                address.getLastName(),
                address.getPhoneNumber(),
                address.getCity(),
                address.getDistrict(),
                address.getNeighborhood(),
                address.getAddressLine(),
                address.getPostalCode(),
                address.getCompanyName(),
                address.getTaxNumber(),
                address.getTaxOffice()
        );
    }

    public static OrderStatusHistoryResponse toOrderStatusHistoryResponse(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getNote(),
                history.getChangedBy(),
                history.getCreatedAt()
        );
    }

    public static OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(OrderMapper::toOrderItemResponse)
                .toList();

        OrderAddress deliveryAddress = order.getAddresses().stream()
                .filter(a -> a.getAddressType() == OrderAddressType.DELIVERY)
                .findFirst()
                .orElse(null);

        OrderAddress billingAddress = order.getAddresses().stream()
                .filter(a -> a.getAddressType() == OrderAddressType.BILLING)
                .findFirst()
                .orElse(null);

        List<OrderStatusHistoryResponse> historyResponses = order.getStatusHistory().stream()
                .map(OrderMapper::toOrderStatusHistoryResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getGuestEmail(),
                order.getGuestFirstName(),
                order.getGuestLastName(),
                order.getGuestPhoneNumber(),
                order.getStatus(),
                order.getCurrency(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getDiscountTotal(),
                order.getGrandTotal(),
                order.getCustomerNotes(),
                order.getAdminNotes(),
                itemResponses,
                toOrderAddressResponse(deliveryAddress),
                toOrderAddressResponse(billingAddress),
                historyResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public static OrderSummaryResponse toOrderSummaryResponse(Order order) {
        int totalItems = order.getItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                totalItems,
                order.getGrandTotal(),
                order.getCurrency(),
                order.getCreatedAt()
        );
    }
}
