package com.carmats.shipping.service;

import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.order.entity.Order;
import com.carmats.order.entity.OrderAddress;
import com.carmats.order.entity.OrderAddressType;
import com.carmats.order.entity.OrderStatus;
import com.carmats.order.repository.OrderRepository;
import com.carmats.shipping.dto.request.CreateShipmentRequest;
import com.carmats.shipping.dto.request.UpdateShipmentStatusRequest;
import com.carmats.shipping.dto.response.ShipmentResponse;
import com.carmats.shipping.entity.Shipment;
import com.carmats.shipping.entity.ShipmentStatus;
import com.carmats.shipping.mapper.ShipmentMapper;
import com.carmats.shipping.repository.ShipmentRepository;
import com.carmats.shipping.repository.ShipmentTrackingEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Transactional
public class ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingEventRepository shipmentTrackingEventRepository;
    private final OrderRepository orderRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ShippingService(
            ShipmentRepository shipmentRepository,
            ShipmentTrackingEventRepository shipmentTrackingEventRepository,
            OrderRepository orderRepository
    ) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentTrackingEventRepository = shipmentTrackingEventRepository;
        this.orderRepository = orderRepository;
    }

    public ShipmentResponse createShipment(CreateShipmentRequest request, String adminEmail) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));

        if (shipmentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BusinessException("SHIPMENT_ALREADY_EXISTS", "Bu sipariş için zaten bir kargo kaydı mevcuttur.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
            throw new BusinessException("ORDER_CANCELLED", "İptal edilmiş veya iade edilmiş sipariş için kargo oluşturulamaz.");
        }

        OrderAddress deliveryAddress = order.getAddresses().stream()
                .filter(a -> a.getAddressType() == OrderAddressType.DELIVERY)
                .findFirst()
                .orElseGet(() -> order.getAddresses().stream().findFirst().orElse(null));

        String recipientName;
        String recipientPhone;
        String addressLine;
        String city;
        String district;

        if (deliveryAddress != null) {
            recipientName = deliveryAddress.getFirstName() + " " + deliveryAddress.getLastName();
            recipientPhone = deliveryAddress.getPhoneNumber();
            addressLine = deliveryAddress.getAddressLine();
            city = deliveryAddress.getCity();
            district = deliveryAddress.getDistrict();
        } else {
            recipientName = (order.getGuestFirstName() != null ? order.getGuestFirstName() : "Müşteri") + " " +
                    (order.getGuestLastName() != null ? order.getGuestLastName() : "");
            recipientPhone = order.getGuestPhoneNumber() != null ? order.getGuestPhoneNumber() : "0000000000";
            addressLine = "Teslimat Adresi";
            city = "İstanbul";
            district = "Merkez";
        }

        String trackingNumber = request.trackingNumber();
        if (trackingNumber == null || trackingNumber.isBlank()) {
            trackingNumber = generateUniqueTrackingNumber();
        }

        String trackingUrl = "https://kargo.carmats.local/track/" + trackingNumber;
        LocalDateTime estDelivery = request.estimatedDeliveryDate() != null
                ? request.estimatedDeliveryDate()
                : LocalDateTime.now().plusDays(3);

        Shipment shipment = new Shipment(
                order,
                request.carrier(),
                trackingNumber,
                recipientName,
                recipientPhone,
                addressLine,
                city,
                district,
                estDelivery,
                trackingUrl
        );

        shipment.addTrackingEvent(
                ShipmentStatus.CREATED,
                "Merkez Depo",
                "Kargo kaydı oluşturuldu, paket hazırlanıyor."
        );

        if (order.getStatus() != OrderStatus.SHIPPED && order.getStatus() != OrderStatus.DELIVERED) {
            order.addStatusHistory(
                    order.getStatus(),
                    OrderStatus.SHIPPED,
                    "Kargoya verildi: " + request.carrier() + " (Takip No: " + trackingNumber + ")",
                    adminEmail != null ? adminEmail : "ADMIN"
            );
            order.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
        }

        Shipment saved = shipmentRepository.save(shipment);
        return ShipmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByOrderNumber(UUID userId, String orderNumber) {
        Order order;
        if (userId != null) {
            order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
                    .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        } else {
            order = orderRepository.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        }

        Shipment shipment = shipmentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new NotFoundException("SHIPMENT_NOT_FOUND", "Kargo kaydı bulunamadı."));

        return ShipmentMapper.toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new NotFoundException("SHIPMENT_NOT_FOUND", "Kargo kaydı bulunamadı."));

        return ShipmentMapper.toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new NotFoundException("SHIPMENT_NOT_FOUND", "Kargo kaydı bulunamadı."));

        return ShipmentMapper.toResponse(shipment);
    }

    public ShipmentResponse updateShipmentStatus(UUID shipmentId, UpdateShipmentStatusRequest request, String changedBy) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new NotFoundException("SHIPMENT_NOT_FOUND", "Kargo kaydı bulunamadı."));

        shipment.addTrackingEvent(request.status(), request.location(), request.description());

        if (request.status() == ShipmentStatus.DELIVERED) {
            Order order = shipment.getOrder();
            if (order.getStatus() != OrderStatus.DELIVERED) {
                order.addStatusHistory(
                        order.getStatus(),
                        OrderStatus.DELIVERED,
                        "Sipariş teslim edildi: " + request.description(),
                        changedBy != null ? changedBy : "ADMIN"
                );
                order.setStatus(OrderStatus.DELIVERED);
                orderRepository.save(order);
            }
        }

        Shipment saved = shipmentRepository.save(shipment);
        return ShipmentMapper.toResponse(saved);
    }

    private String generateUniqueTrackingNumber() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String prefix = "TRK-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        String trackingNumber;
        do {
            StringBuilder sb = new StringBuilder(prefix);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
            }
            trackingNumber = sb.toString();
        } while (shipmentRepository.existsByTrackingNumber(trackingNumber));
        return trackingNumber;
    }
}
