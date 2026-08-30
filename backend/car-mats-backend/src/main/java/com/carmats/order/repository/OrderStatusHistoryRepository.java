package com.carmats.order.repository;

import com.carmats.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {

    List<OrderStatusHistory> findAllByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
