-- PostgreSQL-specific sample data for demonstration
-- This file is executed when using the 'postgres' profile

-- Insert sample customers (using explicit IDs for consistency)
INSERT INTO customers (id, name, email, street, city, postal_code, country) VALUES
(1, 'Max Mustermann', 'max.mustermann@example.com', 'Musterstraße 1', 'Hamburg', '20095', 'Deutschland'),
(2, 'Anna Schmidt', 'anna.schmidt@example.com', 'Beispielweg 2', 'Berlin', '10115', 'Deutschland'),
(3, 'Peter Müller', 'peter.mueller@example.com', 'Teststraße 3', 'München', '80331', 'Deutschland')
ON CONFLICT (id) DO NOTHING;

-- Update the sequence to start after our inserted values
SELECT setval(pg_get_serial_sequence('customers', 'id'), (SELECT MAX(id) FROM customers));

-- Insert sample products (using explicit IDs for consistency)
INSERT INTO products (id, name, description, price, stock_quantity) VALUES
(1, 'Laptop', 'Hochleistungs-Laptop für Entwickler', 1299.99, 50),
(2, 'Smartphone', 'Neuestes Smartphone-Modell', 799.99, 30),
(3, 'Tablet', 'Tablet für Multimedia und Produktivität', 399.99, 25),
(4, 'Kopfhörer', 'Kabellose Bluetooth-Kopfhörer', 149.99, 100),
(5, 'Monitor', '27-Zoll 4K Monitor', 299.99, 15)
ON CONFLICT (id) DO NOTHING;

-- Update the sequence to start after our inserted values
SELECT setval(pg_get_serial_sequence('products', 'id'), (SELECT MAX(id) FROM products));

-- Insert sample orders
INSERT INTO orders (id, customer_id, status, total_amount, currency, created_at, updated_at) VALUES
(1, 1, 'PENDING', 1299.99, 'EUR', NOW(), NOW()),
(2, 2, 'CONFIRMED', 949.98, 'EUR', NOW(), NOW()),
(3, 1, 'PAID', 399.99, 'EUR', NOW(), NOW()),
(4, 3, 'SHIPPED', 449.98, 'EUR', NOW(), NOW()),
(5, 2, 'DELIVERED', 299.99, 'EUR', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Update the sequence to start after our inserted values
SELECT setval(pg_get_serial_sequence('orders', 'id'), (SELECT MAX(id) FROM orders));

-- Insert sample order items
INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price, total_price) VALUES
(1, 1, 1, 'Laptop', 1, 1299.99, 1299.99),
(2, 2, 2, 'Smartphone', 1, 799.99, 799.99),
(3, 2, 4, 'Kopfhörer', 1, 149.99, 149.99),
(4, 3, 3, 'Tablet', 1, 399.99, 399.99),
(5, 4, 4, 'Kopfhörer', 2, 149.99, 299.98),
(6, 4, 4, 'Kopfhörer', 1, 149.99, 149.99),
(7, 5, 5, 'Monitor', 1, 299.99, 299.99)
ON CONFLICT (id) DO NOTHING;

-- Update the sequence to start after our inserted values
SELECT setval(pg_get_serial_sequence('order_items', 'id'), (SELECT MAX(id) FROM order_items));

-- Note: Timestamps are handled by the application (Spring/JPA) using LocalDateTime.now()