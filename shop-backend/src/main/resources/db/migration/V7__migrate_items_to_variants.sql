INSERT INTO item_variants (item_id, stock_quantity)
SELECT id, stock_quantity
FROM items;


ALTER TABLE cart_items
    ADD COLUMN variant_id BIGINT;

UPDATE cart_items ci
SET variant_id = iv.id
FROM item_variants iv
WHERE iv.item_id = ci.item_id;

ALTER TABLE cart_items
    ALTER COLUMN variant_id SET NOT NULL;

ALTER TABLE cart_items
    DROP CONSTRAINT cart_items_pkey;

ALTER TABLE cart_items
    DROP CONSTRAINT fk_cart_items_item;

ALTER TABLE cart_items
    DROP COLUMN item_id;

ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_variant
        FOREIGN KEY (variant_id)
        REFERENCES item_variants(id)
        ON DELETE CASCADE;

ALTER TABLE cart_items
    ADD PRIMARY KEY (cart_id, variant_id);

CREATE INDEX idx_cart_items_variant_id
    ON cart_items(variant_id);


ALTER TABLE order_item
    ADD COLUMN variant_id BIGINT;


UPDATE order_item oi
SET variant_id = iv.id
FROM item_variants iv
WHERE iv.item_id = oi.item_id;

ALTER TABLE order_item
    ALTER COLUMN variant_id SET NOT NULL;


ALTER TABLE order_item
    DROP CONSTRAINT order_item_pkey;

ALTER TABLE order_item
    DROP CONSTRAINT fk_order_item_item;

ALTER TABLE order_item
    DROP COLUMN item_id;

ALTER TABLE order_item
    ADD CONSTRAINT fk_order_item_variant
        FOREIGN KEY (variant_id)
        REFERENCES item_variants(id)
        ON DELETE RESTRICT;

ALTER TABLE order_item
    ADD PRIMARY KEY (order_id, variant_id);

CREATE INDEX idx_order_item_variant_id
    ON order_item(variant_id);


ALTER TABLE items
    DROP COLUMN stock_quantity;