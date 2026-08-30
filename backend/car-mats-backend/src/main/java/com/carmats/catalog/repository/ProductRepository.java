package com.carmats.catalog.repository;

import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository
        extends JpaRepository<Product, UUID> {

    Optional<Product> findBySlugAndStatus(
            String slug,
            ProductStatus status
    );

    List<Product> findAllByStatusOrderByCreatedAtDesc(
            ProductStatus status
    );

    List<Product> findAllByCategoryIdAndStatusOrderByCreatedAtDesc(
            UUID categoryId,
            ProductStatus status
    );
}