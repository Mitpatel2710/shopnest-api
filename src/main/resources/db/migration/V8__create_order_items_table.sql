CREATE TABLE IF NOT EXISTS order_items (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    order_id            BIGINT          NOT NULL,
    product_id          BIGINT          NOT NULL,
    product_name        VARCHAR(200)    NOT NULL,
    price_at_purchase   DECIMAL(10,2)   NOT NULL,
    quantity            INT             NOT NULL,

    CONSTRAINT pk_order_items         PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id)
        REFERENCES products(id),
    CONSTRAINT chk_order_items_qty    CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order   ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);