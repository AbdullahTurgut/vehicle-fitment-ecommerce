package com.carmats.cart.repository;

import com.carmats.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("""
            select distinct c from Cart c
            left join fetch c.items i
            left join fetch i.product p
            left join fetch i.vehicleVariant vv
            where c.user.id = :userId
            """)
    Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);

    @Query("""
            select distinct c from Cart c
            left join fetch c.items i
            left join fetch i.product p
            left join fetch i.vehicleVariant vv
            where c.guestToken = :guestToken
            """)
    Optional<Cart> findByGuestTokenWithItems(@Param("guestToken") String guestToken);

    Optional<Cart> findByUserId(UUID userId);

    Optional<Cart> findByGuestToken(String guestToken);
}
