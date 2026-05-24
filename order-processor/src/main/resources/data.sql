TRUNCATE TABLE orders RESTART IDENTITY;

INSERT INTO orders (amount, status, version, original_order_id, created_at)
SELECT
    (random() * 990 + 10)::numeric(10,2),
    'PENDING',
    0,
    null,
    now()
FROM generate_series(1, 10000);
