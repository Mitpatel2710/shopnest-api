-- V10__seed_initial_data.sql
-- Seed data for ShopNest — development and testing only

-- ── Users ─────────────────────────────────────────────────────────
-- Passwords are BCrypt hashed — plain text: Admin@123, Seller@123, Customer@123
INSERT INTO users (first_name, last_name, email, password, phone, role, active)
VALUES
    ('Admin',   'ShopNest',  'admin@shopnest.com',    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4tbqIy0PYe', '9999999999', 'ADMIN',    true),
    ('Rahul',   'Sharma',    'seller@shopnest.com',   '$2a$12$NmW9x8KQZX1oHxIkLpVpyOJJ7oRGJj6NqaKZ9kL6DlHjMn8cQzEDC', '9876543210', 'SELLER',   true),
    ('Priya',   'Patel',     'customer@shopnest.com', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',  '9123456789', 'CUSTOMER', true),
    ('Arjun',   'Singh',     'arjun@shopnest.com',    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',  '9012345678', 'CUSTOMER', true);

-- ── Root Categories ───────────────────────────────────────────────
INSERT INTO categories (name, slug, description, active)
VALUES
    ('Electronics',   'electronics',  'Electronic gadgets and devices',   true),
    ('Clothing',      'clothing',     'Fashion and apparel',              true),
    ('Books',         'books',        'Books, ebooks and study material', true),
    ('Home & Kitchen','home-kitchen', 'Home appliances and kitchen items',true);

-- ── Sub-categories (use variables to avoid self-reference error) ──
SET @electronics_id  = (SELECT id FROM categories WHERE slug = 'electronics');
SET @clothing_id     = (SELECT id FROM categories WHERE slug = 'clothing');

INSERT INTO categories (name, slug, description, active, parent_id)
VALUES
    ('Mobiles', 'mobiles', 'Smartphones and accessories', true, @electronics_id),
    ('Laptops', 'laptops', 'Laptops and accessories',     true, @electronics_id),
    ('Men',     'men',     'Men clothing',                true, @clothing_id),
    ('Women',   'women',   'Women clothing',              true, @clothing_id);

-- ── Products ──────────────────────────────────────────────────────
SET @mobiles_id      = (SELECT id FROM categories WHERE slug = 'mobiles');
SET @laptops_id      = (SELECT id FROM categories WHERE slug = 'laptops');
SET @men_id          = (SELECT id FROM categories WHERE slug = 'men');
SET @books_id        = (SELECT id FROM categories WHERE slug = 'books');
SET @home_id         = (SELECT id FROM categories WHERE slug = 'home-kitchen');
SET @seller_id       = (SELECT id FROM users WHERE email = 'seller@shopnest.com');

INSERT INTO products (name, description, price, stock_qty, active, type, brand, category_id, seller_id)
VALUES
    ('iPhone 15',         'Apple iPhone 15 with A16 Bionic chip, 48MP camera',          79999.00, 50,  true, 'Electronics',   'Apple',       @mobiles_id, @seller_id),
    ('Samsung Galaxy S24','Samsung Galaxy S24 with Snapdragon 8 Gen 3, 200MP camera',   74999.00, 35,  true, 'Electronics',   'Samsung',     @mobiles_id, @seller_id),
    ('MacBook Air M3',    'Apple MacBook Air with M3 chip, 8GB RAM, 256GB SSD',        114999.00, 20,  true, 'Electronics',   'Apple',       @laptops_id, @seller_id),
    ('Dell XPS 15',       'Dell XPS 15 with Intel Core i7, 16GB RAM, OLED display',   149999.00, 15,  true, 'Electronics',   'Dell',        @laptops_id, @seller_id),
    ('Nike Air Max 270',  'Nike Air Max 270 running shoes — lightweight, breathable',    8999.00, 100, true, 'Clothing',      'Nike',        @men_id,     @seller_id),
    ('Levis 511 Slim',    'Levis 511 slim fit jeans — stretch denim, 5-pocket design',  3499.00, 80,  true, 'Clothing',      'Levis',       @men_id,     @seller_id),
    ('Clean Code',        'Clean Code by Robert C. Martin — Agile Software handbook',    699.00,  200, true, 'Books',         'Pearson',     @books_id,   @seller_id),
    ('Instant Pot Duo',   'Instant Pot 7-in-1 Electric Pressure Cooker 5.7 litre',     7999.00, 40,  true, 'Home & Kitchen','Instant Pot', @home_id,    @seller_id);

-- ── Carts ─────────────────────────────────────────────────────────
SET @customer_id = (SELECT id FROM users WHERE email = 'customer@shopnest.com');
SET @arjun_id    = (SELECT id FROM users WHERE email = 'arjun@shopnest.com');

INSERT INTO carts (user_id)
VALUES (@customer_id), (@arjun_id);

-- ── Cart Items ────────────────────────────────────────────────────
SET @customer_cart_id = (SELECT id FROM carts WHERE user_id = @customer_id);
SET @arjun_cart_id    = (SELECT id FROM carts WHERE user_id = @arjun_id);
SET @iphone_id        = (SELECT id FROM products WHERE name = 'iPhone 15');
SET @cleancode_id     = (SELECT id FROM products WHERE name = 'Clean Code');
SET @macbook_id       = (SELECT id FROM products WHERE name = 'MacBook Air M3');

INSERT INTO cart_items (cart_id, product_id, quantity)
VALUES
    (@customer_cart_id, @iphone_id,   1),
    (@customer_cart_id, @cleancode_id,2),
    (@arjun_cart_id,    @macbook_id,  1);

-- ── Orders ────────────────────────────────────────────────────────
INSERT INTO orders (user_id, status, total_amount, delivery_street, delivery_city,
                    delivery_state, delivery_pincode, payment_method, payment_status)
VALUES
    (@customer_id, 'DELIVERED', 80698.00,  '42, MG Road',    'Bengaluru', 'Karnataka',   '560001', 'UPI',        'SUCCESS'),
    (@customer_id, 'PENDING',   74999.00,  '42, MG Road',    'Bengaluru', 'Karnataka',   '560001', 'CARD',       'PENDING'),
    (@arjun_id,    'SHIPPED',   114999.00, '15, Park Street','Kolkata',   'West Bengal', '700016', 'NETBANKING', 'SUCCESS');

-- ── Order Items ───────────────────────────────────────────────────
SET @samsung_id       = (SELECT id FROM products WHERE name = 'Samsung Galaxy S24');
SET @delivered_order  = (SELECT id FROM orders WHERE user_id = @customer_id AND status = 'DELIVERED');
SET @pending_order    = (SELECT id FROM orders WHERE user_id = @customer_id AND status = 'PENDING');
SET @shipped_order    = (SELECT id FROM orders WHERE user_id = @arjun_id    AND status = 'SHIPPED');

INSERT INTO order_items (order_id, product_id, product_name, price_at_purchase, quantity)
VALUES
    (@delivered_order, @iphone_id,   'iPhone 15',          79999.00, 1),
    (@delivered_order, @cleancode_id,'Clean Code',           699.00, 1),
    (@pending_order,   @samsung_id,  'Samsung Galaxy S24', 74999.00, 1),
    (@shipped_order,   @macbook_id,  'MacBook Air M3',    114999.00, 1);

-- ── Payments ──────────────────────────────────────────────────────
INSERT INTO payments (order_id, transaction_id, amount, method, status, paid_at)
VALUES
    (@delivered_order, 'UPI-TXN-A1B2C3D4', 80698.00,  'UPI',        'SUCCESS', NOW()),
    (@shipped_order,   'NET-TXN-E5F6G7H8', 114999.00, 'NETBANKING', 'SUCCESS', NOW());

-- ── Reviews ───────────────────────────────────────────────────────
INSERT INTO reviews (user_id, product_id, rating, title, comment, verified)
VALUES
    (@customer_id, @iphone_id,   5, 'Absolutely love it!',           'Best phone I have ever used. Camera quality is outstanding.',          true),
    (@customer_id, @cleancode_id,5, 'Must read for every developer', 'Changed the way I write code. Every developer should read this.',      true),
    (@arjun_id,    @macbook_id,  4, 'Great laptop, slightly pricey', 'Performance is incredible for M3 chip. Battery life is amazing.',      false);