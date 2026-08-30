package com.carmats.catalog.repository;
import com.carmats.catalog.entity.ProductFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductFeatureRepository
        extends JpaRepository<ProductFeature, UUID> {

    List<ProductFeature>
    findAllByProductIdOrderBySortOrderAsc(UUID productId);

    Optional<ProductFeature> findByIdAndProductId(UUID id, UUID productId);
}
