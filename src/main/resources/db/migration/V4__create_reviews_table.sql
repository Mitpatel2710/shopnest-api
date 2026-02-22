CREATE TABLE IF NOT EXISTS reviews (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    product_id  BIGINT          NOT NULL,
    rating      INT             NOT NULL,
    title       VARCHAR(200),
    comment     TEXT,
    verified    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_reviews           PRIMARY KEY (id),
    CONSTRAINT uq_reviews_user_product UNIQUE (user_id, product_id),
    CONSTRAINT fk_reviews_user      FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT fk_reviews_product   FOREIGN KEY (product_id)
        REFERENCES products(id),
    CONSTRAINT chk_reviews_rating   CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_user    ON reviews(user_id);