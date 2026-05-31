CREATE TABLE payment (
    id CHAR(36) NOT NULL,
    order_id CHAR(36) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    order_code BIGINT NOT NULL,
    response_data TEXT,
    created_at DATETIME NOT NULL,
    paid_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_order (order_id),
    UNIQUE KEY uk_payment_order_code (order_code),
    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
) ENGINE=InnoDB;
