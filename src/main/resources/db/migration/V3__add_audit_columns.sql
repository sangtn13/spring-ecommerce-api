ALTER TABLE role
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by VARCHAR(191) NULL,
    ADD COLUMN updated_by VARCHAR(191) NULL;

UPDATE role
SET created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW());

ALTER TABLE role
    MODIFY COLUMN created_at DATETIME NOT NULL,
    MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE `user`
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by VARCHAR(191) NULL,
    ADD COLUMN updated_by VARCHAR(191) NULL;

UPDATE `user`
SET created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW());

ALTER TABLE `user`
    MODIFY COLUMN created_at DATETIME NOT NULL,
    MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE category
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by VARCHAR(191) NULL,
    ADD COLUMN updated_by VARCHAR(191) NULL;

UPDATE category
SET created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW());

ALTER TABLE category
    MODIFY COLUMN created_at DATETIME NOT NULL,
    MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE product
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by VARCHAR(191) NULL,
    ADD COLUMN updated_by VARCHAR(191) NULL;

UPDATE product
SET created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW());

ALTER TABLE product
    MODIFY COLUMN created_at DATETIME NOT NULL,
    MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE image
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by VARCHAR(191) NULL,
    ADD COLUMN updated_by VARCHAR(191) NULL;

UPDATE image
SET created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW());

ALTER TABLE image
    MODIFY COLUMN created_at DATETIME NOT NULL,
    MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE cart
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by VARCHAR(191) NULL,
    ADD COLUMN updated_by VARCHAR(191) NULL;

UPDATE cart
SET created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW());

ALTER TABLE cart
    MODIFY COLUMN created_at DATETIME NOT NULL,
    MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE cart_item
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by VARCHAR(191) NULL,
    ADD COLUMN updated_by VARCHAR(191) NULL;

UPDATE cart_item
SET created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW());

ALTER TABLE cart_item
    MODIFY COLUMN created_at DATETIME NOT NULL,
    MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE orders
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by VARCHAR(191) NULL,
    ADD COLUMN updated_by VARCHAR(191) NULL;

UPDATE orders
SET created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW());

ALTER TABLE orders
    MODIFY COLUMN created_at DATETIME NOT NULL,
    MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE order_item
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by VARCHAR(191) NULL,
    ADD COLUMN updated_by VARCHAR(191) NULL;

UPDATE order_item
SET created_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(updated_at, NOW());

ALTER TABLE order_item
    MODIFY COLUMN created_at DATETIME NOT NULL,
    MODIFY COLUMN updated_at DATETIME NOT NULL;

ALTER TABLE payment
    ADD COLUMN updated_at DATETIME NULL,
    ADD COLUMN created_by VARCHAR(191) NULL,
    ADD COLUMN updated_by VARCHAR(191) NULL;

UPDATE payment
SET updated_at = COALESCE(updated_at, created_at);

ALTER TABLE payment
    MODIFY COLUMN updated_at DATETIME NOT NULL;
