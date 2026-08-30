CREATE TABLE categories
(
    id           UUID PRIMARY KEY,
    parent_id    UUID,
    name         VARCHAR(120) NOT NULL,
    slug         VARCHAR(150) NOT NULL UNIQUE,
    description  TEXT,
    image_url    VARCHAR(500),
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order   INTEGER NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id)
            REFERENCES categories(id)
);

CREATE TABLE products
(
    id                  UUID PRIMARY KEY,
    category_id         UUID NOT NULL,
    name                VARCHAR(200) NOT NULL,
    slug                VARCHAR(220) NOT NULL UNIQUE,
    sku                 VARCHAR(100) NOT NULL UNIQUE,
    short_description   VARCHAR(500),
    description         TEXT,
    base_price          NUMERIC(12,2) NOT NULL,
    sale_price          NUMERIC(12,2),
    stock_quantity      INTEGER NOT NULL DEFAULT 0,
    status              VARCHAR(30) NOT NULL,
    featured            BOOLEAN NOT NULL DEFAULT FALSE,
    manufacturer_brand  VARCHAR(120),
    material            VARCHAR(120),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id)
            REFERENCES categories(id),

    CONSTRAINT chk_product_base_price
        CHECK (base_price >= 0),

    CONSTRAINT chk_product_sale_price
        CHECK (sale_price IS NULL OR sale_price >= 0),

    CONSTRAINT chk_product_stock
        CHECK (stock_quantity >= 0)
);

CREATE TABLE product_images
(
    id          UUID PRIMARY KEY,
    product_id  UUID NOT NULL,
    url         VARCHAR(500) NOT NULL,
    alt_text    VARCHAR(250),
    sort_order  INTEGER NOT NULL DEFAULT 0,
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_image_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
);

CREATE TABLE product_features
(
    id           UUID PRIMARY KEY,
    product_id   UUID NOT NULL,
    title        VARCHAR(150) NOT NULL,
    description  VARCHAR(500),
    icon         VARCHAR(100),
    sort_order   INTEGER NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_feature_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
);

CREATE TABLE product_compatibilities
(
    id                  UUID PRIMARY KEY,
    product_id          UUID NOT NULL,
    vehicle_variant_id  UUID NOT NULL,
    start_year          INTEGER,
    end_year            INTEGER,
    notes               TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_compatibility_product
        FOREIGN KEY (product_id)
            REFERENCES products(id),

    CONSTRAINT fk_product_compatibility_vehicle_variant
        FOREIGN KEY (vehicle_variant_id)
            REFERENCES vehicle_variants(id),

    CONSTRAINT chk_product_compatibility_years
        CHECK (
            start_year IS NULL
                OR end_year IS NULL
                OR end_year >= start_year
            ),

    CONSTRAINT uk_product_compatibility
        UNIQUE (
                product_id,
                vehicle_variant_id,
                start_year,
                end_year
            )
);

CREATE INDEX idx_products_category_id
    ON products(category_id);

CREATE INDEX idx_products_status
    ON products(status);

CREATE INDEX idx_products_featured
    ON products(featured);

CREATE INDEX idx_product_images_product_id
    ON product_images(product_id);

CREATE INDEX idx_product_features_product_id
    ON product_features(product_id);

CREATE INDEX idx_product_compatibility_product_id
    ON product_compatibilities(product_id);

CREATE INDEX idx_product_compatibility_variant_id
    ON product_compatibilities(vehicle_variant_id);

CREATE INDEX idx_product_compatibility_variant_year
    ON product_compatibilities(
                               vehicle_variant_id,
                               start_year,
                               end_year
        );