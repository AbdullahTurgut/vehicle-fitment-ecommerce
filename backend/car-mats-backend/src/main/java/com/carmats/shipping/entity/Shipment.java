package com.carmats.shipping.entity;

import com.carmats.common.entity.BaseEntity;
import com.carmats.order.entity.Order;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments")
public class Shipment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ShippingCarrier carrier;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status = ShipmentStatus.CREATED;

    @Column(name = "recipient_name", nullable = false, length = 200)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 30)
    private String recipientPhone;

    @Column(name = "delivery_address_line", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddressLine;

    @Column(name = "delivery_city", nullable = false, length = 100)
    private String deliveryCity;

    @Column(name = "delivery_district", nullable = false, length = 100)
    private String deliveryDistrict;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "tracking_url", length = 500)
    private String trackingUrl;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("eventTime DESC")
    private List<ShipmentTrackingEvent> trackingEvents = new ArrayList<>();

    protected Shipment() {
    }

    public Shipment(
            Order order,
            ShippingCarrier carrier,
            String trackingNumber,
            String recipientName,
            String recipientPhone,
            String deliveryAddressLine,
            String deliveryCity,
            String deliveryDistrict,
            LocalDateTime estimatedDeliveryDate,
            String trackingUrl
    ) {
        this.order = order;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.deliveryAddressLine = deliveryAddressLine;
        this.deliveryCity = deliveryCity;
        this.deliveryDistrict = deliveryDistrict;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.trackingUrl = trackingUrl;
        this.status = ShipmentStatus.CREATED;
        this.shippedAt = LocalDateTime.now();
    }

    public void addTrackingEvent(ShipmentStatus status, String location, String description) {
        ShipmentTrackingEvent event = new ShipmentTrackingEvent(this, status, location, description);
        this.trackingEvents.add(event);
        this.status = status;
        if (status == ShipmentStatus.DELIVERED) {
            this.deliveredAt = LocalDateTime.now();
        }
    }

    public Order getOrder() {
        return order;
    }

    public ShippingCarrier getCarrier() {
        return carrier;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public String getDeliveryAddressLine() {
        return deliveryAddressLine;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public String getDeliveryDistrict() {
        return deliveryDistrict;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public List<ShipmentTrackingEvent> getTrackingEvents() {
        return trackingEvents;
    }
}
