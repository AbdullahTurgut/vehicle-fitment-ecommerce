CREATE TABLE carts
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID REFERENCES users (id) ON DELETE CASCADE,
    guest_token VARCHAR(100),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_carts_unique_user ON carts (user_id) WHERE user_id IS NOT NULL;
CREATE UNIQUE INDEX idx_carts_unique_guest ON carts (guest_token) WHERE guest_token IS NOT NULL;

CREATE TABLE cart_items
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id            UUID           NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id         UUID           NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    vehicle_variant_id UUID REFERENCES vehicle_variants (id) ON DELETE SET NULL,
    quantity           INTEGER        NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price         NUMERIC(10, 2) NOT NULL,
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items (product_id);
