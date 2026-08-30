package com.carmats.catalog.repository;

import com.carmats.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository
        extends JpaRepository<Category, UUID> {

    List<Category> findAllByActiveTrueOrderBySortOrderAscNameAsc();

    Optional<Category> findBySlugAndActiveTrue(String slug);

    boolean existsByIdAndActiveTrue(UUID id);
}