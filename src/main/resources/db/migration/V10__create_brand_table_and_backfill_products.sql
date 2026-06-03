CREATE TABLE brand (
    id CHAR(36) NOT NULL,
    name VARCHAR(120) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(191) NULL,
    updated_by VARCHAR(191) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_brand_name (name)
) ENGINE=InnoDB;

INSERT INTO brand (id, name, created_at, updated_at, created_by, updated_by)
SELECT UUID(), TRIM(p.brand), NOW(), NOW(), 'system', 'system'
FROM product p
WHERE p.brand IS NOT NULL
  AND TRIM(p.brand) <> ''
GROUP BY TRIM(p.brand);

ALTER TABLE product
    ADD COLUMN brand_id CHAR(36) NULL,
    ADD KEY idx_product_brand_id (brand_id),
    ADD KEY idx_product_name_prefix (name),
    ADD KEY idx_product_price (price),
    ADD KEY idx_product_brand_category_price (brand_id, category_id, price);

UPDATE product p
JOIN brand b ON b.name = TRIM(p.brand)
SET p.brand_id = b.id
WHERE p.brand_id IS NULL
  AND p.brand IS NOT NULL
  AND TRIM(p.brand) <> '';

ALTER TABLE product
    ADD CONSTRAINT fk_product_brand
        FOREIGN KEY (brand_id)
        REFERENCES brand (id);
