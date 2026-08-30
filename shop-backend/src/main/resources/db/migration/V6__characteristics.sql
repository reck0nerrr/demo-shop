CREATE TABLE characteristic_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE characteristic_values (
    id BIGSERIAL PRIMARY KEY,
    characteristic_type_id BIGINT NOT NULL,
    value VARCHAR(50) NOT NULL,

    CONSTRAINT fk_characteristic_values_type
        FOREIGN KEY (characteristic_type_id)
        REFERENCES characteristic_types(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_characteristic_value
        UNIQUE (characteristic_type_id, value)
);

CREATE INDEX idx_characteristic_values_type_id
    ON characteristic_values(characteristic_type_id);


CREATE TABLE item_characteristic_types (
    item_id BIGINT NOT NULL,
    characteristic_type_id BIGINT NOT NULL,

    PRIMARY KEY (item_id, characteristic_type_id),

    CONSTRAINT fk_ict_item
        FOREIGN KEY (item_id)
        REFERENCES items(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ict_type
        FOREIGN KEY (characteristic_type_id)
        REFERENCES characteristic_types(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_ict_characteristic_type_id
    ON item_characteristic_types(characteristic_type_id);


CREATE TABLE item_variants (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0
        CHECK (stock_quantity >= 0),
    price_override NUMERIC(12, 2)
        CHECK (price_override >= 0),

    CONSTRAINT fk_item_variants_item
        FOREIGN KEY (item_id)
        REFERENCES items(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_item_variants_item_id
    ON item_variants(item_id);


CREATE TABLE item_variant_values (
    variant_id BIGINT NOT NULL,
    characteristic_value_id BIGINT NOT NULL,

    PRIMARY KEY (variant_id, characteristic_value_id),

    CONSTRAINT fk_ivv_variant
        FOREIGN KEY (variant_id)
        REFERENCES item_variants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ivv_value
        FOREIGN KEY (characteristic_value_id)
        REFERENCES characteristic_values(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_ivv_characteristic_value_id
    ON item_variant_values(characteristic_value_id);