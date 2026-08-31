INSERT INTO item_images (item_id, image_url, sort_order)
SELECT id, image_url, 0
FROM items
WHERE image_url IS NOT NULL;

ALTER TABLE items DROP COLUMN image_url;