-- Sample data for HomeTech database
-- Run this script to add sample products and categories

-- Insert categories if they don't exist
INSERT IGNORE INTO categories (category_name) VALUES 
('Smartphone'),
('Laptop'), 
('Tablet'),
('Smart Watch');

-- Insert sample products
INSERT IGNORE INTO products (product_name, price, description, categoryID, sold_count, status, color, size, created_at) VALUES
-- Smartphones
('iPhone 15 Pro', 25990000, 'iPhone 15 Pro với chip A17 Pro mạnh mẽ', 1, 150, 1, 'Black', 0, NOW()),
('Samsung Galaxy S24 Ultra', 28990000, 'Galaxy S24 Ultra với camera 200MP', 1, 120, 1, 'Black', 0, NOW()),
('Google Pixel 9 Pro', 22990000, 'Pixel 9 Pro với AI tích hợp', 1, 80, 1, 'Black', 0, NOW()),
('OnePlus 12', 19990000, 'OnePlus 12 với Snapdragon 8 Gen 3', 1, 60, 1, 'Black', 0, NOW()),

-- Laptops  
('MacBook Pro M3', 45990000, 'MacBook Pro với chip M3 Pro', 2, 90, 1, 'Black', 0, NOW()),
('Dell XPS 15', 35990000, 'Dell XPS 15 với Intel Core i7', 2, 70, 1, 'Black', 0, NOW()),
('ASUS ROG Strix', 32990000, 'ASUS ROG Strix gaming laptop', 2, 50, 1, 'Black', 0, NOW()),

-- Tablets
('iPad Pro 12.9"', 25990000, 'iPad Pro với chip M2', 3, 100, 1, 'Black', 0, NOW()),
('Samsung Galaxy Tab S9', 18990000, 'Galaxy Tab S9 với S Pen', 3, 40, 1, 'Black', 0, NOW()),

-- Smart Watches
('Apple Watch Series 9', 8990000, 'Apple Watch Series 9 với chip S9', 4, 200, 1, 'Black', 0, NOW()),
('Samsung Galaxy Watch 6', 6990000, 'Galaxy Watch 6 với Wear OS', 4, 80, 1, 'Black', 0, NOW()),
('Google Pixel Watch 2', 7990000, 'Pixel Watch 2 với Fitbit', 4, 60, 1, 'Black', 0, NOW());




