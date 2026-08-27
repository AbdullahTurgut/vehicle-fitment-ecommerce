CREATE TABLE vehicle_brands
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    logo_url    VARCHAR(500),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vehicle_models
(
    id          UUID PRIMARY KEY,
    brand_id    UUID NOT NULL,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vehicle_model_brand
        FOREIGN KEY (brand_id)
            REFERENCES vehicle_brands(id),

    CONSTRAINT uk_vehicle_model_brand_slug
        UNIQUE (brand_id, slug)
);

CREATE TABLE vehicle_generations
(
    id          UUID PRIMARY KEY,
    model_id    UUID NOT NULL,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(50),
    start_year  INTEGER,
    end_year    INTEGER,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vehicle_generation_model
        FOREIGN KEY (model_id)
            REFERENCES vehicle_models(id),

    CONSTRAINT chk_vehicle_generation_years
        CHECK (
            end_year IS NULL
                OR start_year IS NULL
                OR end_year >= start_year
            )
);

CREATE TABLE vehicle_variants
(
    id              UUID PRIMARY KEY,
    generation_id   UUID NOT NULL,
    name            VARCHAR(120) NOT NULL,
    body_type       VARCHAR(50),
    fuel_type       VARCHAR(50),
    seat_count      INTEGER,
    trunk_type      VARCHAR(100),
    notes           TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vehicle_variant_generation
        FOREIGN KEY (generation_id)
            REFERENCES vehicle_generations(id),

    CONSTRAINT chk_vehicle_variant_seat_count
        CHECK (
            seat_count IS NULL
                OR seat_count > 0
            )
);

CREATE INDEX idx_vehicle_models_brand_id
    ON vehicle_models(brand_id);

CREATE INDEX idx_vehicle_generations_model_id
    ON vehicle_generations(model_id);

CREATE INDEX idx_vehicle_variants_generation_id
    ON vehicle_variants(generation_id);

CREATE INDEX idx_vehicle_brands_active_sort
    ON vehicle_brands(active, sort_order);