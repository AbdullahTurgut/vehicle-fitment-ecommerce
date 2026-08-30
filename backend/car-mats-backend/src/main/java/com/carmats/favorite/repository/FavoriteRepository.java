package com.carmats.favorite.repository;

import com.carmats.favorite.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    @Query("SELECT f FROM Favorite f JOIN FETCH f.product p LEFT JOIN FETCH p.category WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    Page<Favorite> findAllByUserIdWithProduct(@Param("userId") UUID userId, Pageable pageable);

    Optional<Favorite> findByUserIdAndProductId(UUID userId, UUID productId);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    void deleteByUserIdAndProductId(UUID userId, UUID productId);
}
