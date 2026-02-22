-- V1__create_users_table.sql
-- First migration — create users table

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    first_name  VARCHAR(50)     NOT NULL,
    last_name   VARCHAR(50),
    email       VARCHAR(100)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    phone       VARCHAR(15),
    role        VARCHAR(20)     NOT NULL DEFAULT 'CUSTOMER',
    active      BOOLEAN         NOT NULL DEFAULT TRUE,

    -- Address embedded columns
    street      VARCHAR(255),
    city        VARCHAR(100),
    state       VARCHAR(100),
    pincode     VARCHAR(10),

    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email  ON users(email);
CREATE INDEX idx_users_active ON users(active);