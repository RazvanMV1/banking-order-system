DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    version BIGINT,
    original_order_id BIGINT,
    created_at TIMESTAMP
);
