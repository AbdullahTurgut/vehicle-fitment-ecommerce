package com.carmats.order.entity;

import com.carmats.common.entity.BaseEntity;
import com.carmats.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "guest_email", length = 150)
    private String guestEmail;

    @Column(name = "guest_first_name", length = 100)
    private String guestFirstName;

    @Column(name = "guest_last_name", length = 100)
    private String guestLastName;

    @Column(name = "guest_phone_number", length = 30)
    private String guestPhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(nullable = false, length = 10)
    private String currency = "TRY";

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "discount_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    protected Order() {
    }

    public Order(
            String orderNumber,
            User user,
            String guestEmail,
            String guestFirstName,
            String guestLastName,
            String guestPhoneNumber,
            OrderStatus status,
            BigDecimal subtotal,
            BigDecimal shippingFee,
            BigDecimal discountTotal,
            BigDecimal grandTotal,
            String customerNotes
    ) {
        this.orderNumber = orderNumber;
        this.user = user;
        this.guestEmail = guestEmail;
        this.guestFirstName = guestFirstName;
        this.guestLastName = guestLastName;
        this.guestPhoneNumber = guestPhoneNumber;
        this.status = status != null ? status : OrderStatus.PENDING_PAYMENT;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee != null ? shippingFee : BigDecimal.ZERO;
        this.discountTotal = discountTotal != null ? discountTotal : BigDecimal.ZERO;
        this.grandTotal = grandTotal;
        this.customerNotes = customerNotes;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void addAddress(OrderAddress address) {
        addresses.add(address);
        address.setOrder(this);
    }

    public void addStatusHistory(OrderStatus fromStatus, OrderStatus toStatus, String note, String changedBy) {
        OrderStatusHistory history = new OrderStatusHistory(this, fromStatus, toStatus, note, changedBy);
        statusHistory.add(history);
        this.status = toStatus;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public User getUser() {
        return user;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public String getGuestFirstName() {
        return guestFirstName;
    }

    public String getGuestLastName() {
        return guestLastName;
    }

    public String getGuestPhoneNumber() {
        return guestPhoneNumber;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public BigDecimal getDiscountTotal() {
        return discountTotal;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public List<OrderAddress> getAddresses() {
        return addresses;
    }

    public List<OrderStatusHistory> getStatusHistory() {
        return statusHistory;
    }
}
