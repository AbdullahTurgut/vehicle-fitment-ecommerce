package com.carmats.cart.repository;

import com.carmats.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndProductIdAndVehicleVariantId(UUID cartId, UUID productId, UUID vehicleVariantId);

    Optional<CartItem> findByCartIdAndProductIdAndVehicleVariantIsNull(UUID cartId, UUID productId);

    Optional<CartItem> findByIdAndCartId(UUID id, UUID cartId);

    List<CartItem> findAllByCartId(UUID cartId);
}
