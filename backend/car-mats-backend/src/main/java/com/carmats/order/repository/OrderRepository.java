package com.carmats.order.repository;

import com.carmats.order.entity.Order;
import com.carmats.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByOrderNumberAndUserId(String orderNumber, UUID userId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE (:status IS NULL OR o.status = :status) ORDER BY o.createdAt DESC")
    Page<Order> findAllByStatusFilter(@Param("status") OrderStatus status, Pageable pageable);

    default Optional<Order> findByIdWithDetails(UUID id) {
        return findById(id);
    }

    default Optional<Order> findByOrderNumberWithDetails(String orderNumber) {
        return findByOrderNumber(orderNumber);
    }

    boolean existsByOrderNumber(String orderNumber);
}
