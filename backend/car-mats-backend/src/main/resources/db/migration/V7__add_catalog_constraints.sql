CREATE UNIQUE INDEX uk_product_primary_image
    ON product_images(product_id)
    WHERE is_primary = TRUE;