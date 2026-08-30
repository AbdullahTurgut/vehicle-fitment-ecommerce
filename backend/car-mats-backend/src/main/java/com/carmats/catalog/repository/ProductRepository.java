package com.carmats.catalog.repository;

import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.projection.ProductListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository
        extends JpaRepository<Product, UUID> {

    /**
     * Public ürün detay sayfası için kullanılır.
     *
     * Ürün:
     * - ACTIVE olmalı
     * - kategori aktif olmalı
     */
    @Query("""
        select p
        from Product p
        join fetch p.category c
        where p.slug = :slug
          and p.status = :status
          and c.active = true
        """)
    Optional<Product> findPublicProductBySlug(
            @Param("slug") String slug,
            @Param("status") ProductStatus status
    );


    /**
     * Public katalog ürün listesini getirir.
     *
     * categorySlug null ise tüm aktif kategorilerdeki
     * aktif ürünler listelenir.
     *
     * Primary image aynı query içerisinde alınır.
     */
    @Query(
            value = """
                select
                    p.id as id,
                    p.name as name,
                    p.slug as slug,
                    p.sku as sku,
                    p.base_price as basePrice,
                    p.sale_price as salePrice,
                    p.stock_quantity as stockQuantity,
                    p.featured as featured,
                    pi.url as primaryImageUrl
                from products p

                join categories c
                    on c.id = p.category_id

                left join product_images pi
                    on pi.product_id = p.id
                   and pi.is_primary = true

                where p.status = 'ACTIVE'
                  and c.active = true
                  and (
                        :categorySlug is null
                        or c.slug = :categorySlug
                  )
                """,
            countQuery = """
                select count(*)
                from products p

                join categories c
                    on c.id = p.category_id

                where p.status = 'ACTIVE'
                  and c.active = true
                  and (
                        :categorySlug is null
                        or c.slug = :categorySlug
                  )
                """,
            nativeQuery = true
    )
    Page<ProductListProjection> findPublicProducts(
            @Param("categorySlug") String categorySlug,
            Pageable pageable
    );
}