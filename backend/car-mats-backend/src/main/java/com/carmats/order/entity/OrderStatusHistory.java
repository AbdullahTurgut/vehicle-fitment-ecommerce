package com.carmats.order.entity;

import com.carmats.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private OrderStatus toStatus;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    protected OrderStatusHistory() {
    }

    public OrderStatusHistory(
            Order order,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            String note,
            String changedBy
    ) {
        this.order = order;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.note = note;
        this.changedBy = changedBy;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public OrderStatus getFromStatus() {
        return fromStatus;
    }

    public OrderStatus getToStatus() {
        return toStatus;
    }

    public String getNote() {
        return note;
    }

    public String getChangedBy() {
        return changedBy;
    }
}
