package com.carmats.order.repository;

import com.carmats.order.entity.OrderAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderAddressRepository extends JpaRepository<OrderAddress, UUID> {

    List<OrderAddress> findAllByOrderId(UUID orderId);
}
