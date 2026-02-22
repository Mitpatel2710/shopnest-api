CREATE TABLE IF NOT EXISTS orders (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    user_id           BIGINT          NOT NULL,
    status            VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    total_amount      DECIMAL(10,2)   NOT NULL,
    delivery_street   VARCHAR(255)    NOT NULL,
    delivery_city     VARCHAR(100)    NOT NULL,
    delivery_state    VARCHAR(100)    NOT NULL,
    delivery_pincode  VARCHAR(10)     NOT NULL,
    payment_method    VARCHAR(30),
    payment_status    VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    placed_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                      ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_orders      PRIMARY KEY (id),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_orders_user_id   ON orders(user_id);
CREATE INDEX idx_orders_status    ON orders(status);
CREATE INDEX idx_orders_placed_at ON orders(placed_at);