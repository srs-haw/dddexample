-- Sample data for demonstration

-- Insert sample customers (let JPA auto-generate IDs)
INSERT INTO customers (name, email, street, city, postal_code, country) VALUES
('Max Mustermann', 'max.mustermann@example.com', 'Musterstraße 1', 'Hamburg', '20095', 'Deutschland'),
('Anna Schmidt', 'anna.schmidt@example.com', 'Beispielweg 2', 'Berlin', '10115', 'Deutschland'),
('Peter Müller', 'peter.mueller@example.com', 'Teststraße 3', 'München', '80331', 'Deutschland');

-- Insert sample products (let JPA auto-generate IDs)
INSERT INTO products (name, description, price, stock_quantity) VALUES
('Laptop', 'Hochleistungs-Laptop für Entwickler', 1299.99, 10),
('Smartphone', 'Neuestes Smartphone-Modell', 799.99, 25),
('Tablet', 'Tablet für Multimedia und Produktivität', 399.99, 15),
('Kopfhörer', 'Kabellose Bluetooth-Kopfhörer', 149.99, 50),
('Monitor', '27-Zoll 4K Monitor', 299.99, 8);

