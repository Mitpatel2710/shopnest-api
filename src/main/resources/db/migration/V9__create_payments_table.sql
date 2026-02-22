CREATE TABLE IF NOT EXISTS payments (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    order_id          BIGINT          NOT NULL,
    transaction_id    VARCHAR(100),
    amount            DECIMAL(10,2)   NOT NULL,
    method            VARCHAR(30)     NOT NULL,
    status            VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    gateway_response  TEXT,
    paid_at           TIMESTAMP,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_payments        PRIMARY KEY (id),
    CONSTRAINT uq_payments_order  UNIQUE (order_id),
    CONSTRAINT uq_payments_txn    UNIQUE (transaction_id),
    CONSTRAINT fk_payments_order  FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE
);