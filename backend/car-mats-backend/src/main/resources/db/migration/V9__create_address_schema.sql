CREATE TABLE addresses
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title               VARCHAR(100) NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone_number        VARCHAR(30)  NOT NULL,
    city                VARCHAR(100) NOT NULL,
    district            VARCHAR(100) NOT NULL,
    neighborhood        VARCHAR(150),
    address_line        VARCHAR(500) NOT NULL,
    postal_code         VARCHAR(20),
    company_name        VARCHAR(150),
    tax_number          VARCHAR(50),
    tax_office          VARCHAR(100),
    is_default_delivery BOOLEAN      NOT NULL DEFAULT FALSE,
    is_default_billing  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_addresses_user_id ON addresses (user_id);
