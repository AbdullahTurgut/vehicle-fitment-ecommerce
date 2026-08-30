package com.carmats.catalog.repository;

import com.carmats.catalog.entity.ProductCompatibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductCompatibilityRepository
        extends JpaRepository<ProductCompatibility, UUID> {

    @Query("""
        select pc
        from ProductCompatibility pc
        join fetch pc.product p
        where pc.vehicleVariant.id = :variantId
          and p.status = com.carmats.catalog.entity.ProductStatus.ACTIVE
          and (:year is null
               or (
                    (pc.startYear is null or pc.startYear <= :year)
                    and
                    (pc.endYear is null or pc.endYear >= :year)
               )
          )
        order by p.featured desc, p.name asc
        """)
    List<ProductCompatibility> findCompatibleProducts(
            @Param("variantId") UUID variantId,
            @Param("year") Integer year
    );
}