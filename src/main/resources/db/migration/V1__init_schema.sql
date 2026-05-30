CREATE TABLE role (
    id SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_name (name)
) ENGINE=InnoDB;

CREATE TABLE `user` (
    id CHAR(36) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(191) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_email (email)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
    user_id CHAR(36) NOT NULL,
    role_id SMALLINT UNSIGNED NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES `user` (id),
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES role (id)
) ENGINE=InnoDB;

CREATE TABLE category (
    id CHAR(36) NOT NULL,
    name VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name)
) ENGINE=InnoDB;

CREATE TABLE product (
    id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    brand VARCHAR(120),
    price DECIMAL(12,2) NOT NULL,
    inventory INT NOT NULL,
    description TEXT,
    category_id CHAR(36),
    PRIMARY KEY (id),
    KEY idx_product_category (category_id),
    FULLTEXT KEY ft_product_search (name, brand, description),
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id)
        REFERENCES category (id)
) ENGINE=InnoDB;

CREATE TABLE image (
    id CHAR(36) NOT NULL,
    file_name VARCHAR(200),
    file_type VARCHAR(100),
    image LONGBLOB,
    download_url VARCHAR(500),
    product_id CHAR(36),
    PRIMARY KEY (id),
    KEY idx_image_product (product_id),
    CONSTRAINT fk_image_product
        FOREIGN KEY (product_id)
        REFERENCES product (id)
) ENGINE=InnoDB;

CREATE TABLE cart (
    id CHAR(36) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    user_id CHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_user (user_id),
    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id)
        REFERENCES `user` (id)
) ENGINE=InnoDB;

CREATE TABLE cart_item (
    id CHAR(36) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    product_id CHAR(36),
    cart_id CHAR(36),
    PRIMARY KEY (id),
    KEY idx_cart_item_product (product_id),
    KEY idx_cart_item_cart (cart_id),
    CONSTRAINT fk_cart_item_product
        FOREIGN KEY (product_id)
        REFERENCES product (id),
    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id)
        REFERENCES cart (id)
) ENGINE=InnoDB;

CREATE TABLE orders (
    id CHAR(36) NOT NULL,
    order_date DATE,
    total_amount DECIMAL(12,2) NOT NULL,
    order_status VARCHAR(20) NOT NULL,
    user_id CHAR(36),
    PRIMARY KEY (id),
    KEY idx_orders_user (user_id),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES `user` (id)
) ENGINE=InnoDB;

CREATE TABLE order_item (
    id CHAR(36) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    order_id CHAR(36),
    product_id CHAR(36),
    PRIMARY KEY (id),
    KEY idx_order_item_order (order_id),
    KEY idx_order_item_product (product_id),
    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id),
    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id)
        REFERENCES product (id)
) ENGINE=InnoDB;
