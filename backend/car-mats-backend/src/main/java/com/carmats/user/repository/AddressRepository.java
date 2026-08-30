package com.carmats.user.repository;

import com.carmats.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Address> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);

    @Modifying
    @Query("update Address a set a.defaultDelivery = false where a.user.id = :userId")
    void resetDefaultDelivery(@Param("userId") UUID userId);

    @Modifying
    @Query("update Address a set a.defaultBilling = false where a.user.id = :userId")
    void resetDefaultBilling(@Param("userId") UUID userId);
}
