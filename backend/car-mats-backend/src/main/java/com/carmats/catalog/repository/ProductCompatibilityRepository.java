package com.carmats.catalog.repository;

import com.carmats.catalog.entity.ProductCompatibility;
import com.carmats.catalog.repository.projection.ProductListProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductCompatibilityRepository
        extends JpaRepository<ProductCompatibility, UUID> {

    @Query(
            value = """
                select distinct
                    p.id as id,
                    p.name as name,
                    p.slug as slug,
                    p.sku as sku,
                    p.base_price as basePrice,
                    p.sale_price as salePrice,
                    p.stock_quantity as stockQuantity,
                    p.featured as featured,
                    pi.url as primaryImageUrl
                from product_compatibilities pc

                join products p
                    on p.id = pc.product_id

                left join product_images pi
                    on pi.product_id = p.id
                   and pi.is_primary = true

                where pc.vehicle_variant_id = :variantId
                  and p.status = 'ACTIVE'
                  and (
                        cast(:year as integer) is null
                        or (
                             (pc.start_year is null or pc.start_year <= cast(:year as integer))
                             and
                             (pc.end_year is null or pc.end_year >= cast(:year as integer))
                        )
                  )

                order by p.featured desc, p.name asc
                """,
            nativeQuery = true
    )
    List<ProductListProjection> findCompatibleProducts(
            @Param("variantId") UUID variantId,
            @Param("year") Integer year
    );
}