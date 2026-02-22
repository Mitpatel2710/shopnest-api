CREATE TABLE IF NOT EXISTS products (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(200)    NOT NULL,
    description TEXT,
    price       DECIMAL(10,2)   NOT NULL,
    stock_qty   INT             NOT NULL DEFAULT 0,
    image_url   VARCHAR(500),
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    type        VARCHAR(50),
    brand       VARCHAR(100),
    category_id BIGINT          NOT NULL,
    seller_id   BIGINT,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_products    PRIMARY KEY (id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id)
        REFERENCES categories(id),
    CONSTRAINT fk_products_seller   FOREIGN KEY (seller_id)
        REFERENCES users(id),
    CONSTRAINT chk_products_price   CHECK (price >= 0),
    CONSTRAINT chk_products_stock   CHECK (stock_qty >= 0)
);

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_price    ON products(price);
CREATE INDEX idx_products_active   ON products(active);