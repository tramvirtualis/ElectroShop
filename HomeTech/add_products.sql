-- Add sample products to HomeTech database
-- Run this in your MySQL client

USE HomeTech;

-- Insert sample products directly
INSERT INTO products (product_name, price, description, categoryID, sold_count, status, color, size, created_at) VALUES
('iPhone 15 Pro', 25990000, 'iPhone 15 Pro với chip A17 Pro mạnh mẽ', 1, 150, 1, 'Black', 0, NOW()),
('Samsung Galaxy S24 Ultra', 28990000, 'Galaxy S24 Ultra với camera 200MP', 1, 120, 1, 'Black', 0, NOW()),
('Google Pixel 9 Pro', 22990000, 'Pixel 9 Pro với AI tích hợp', 1, 80, 1, 'Black', 0, NOW()),
('OnePlus 12', 19990000, 'OnePlus 12 với Snapdragon 8 Gen 3', 1, 60, 1, 'Black', 0, NOW()),
('Xiaomi 14 Ultra', 21990000, 'Xiaomi 14 Ultra flagship camera phone', 1, 90, 1, 'Black', 0, NOW()),
('MacBook Pro M3', 45990000, 'MacBook Pro với chip M3 Pro', 2, 90, 1, 'Silver', 0, NOW()),
('Dell XPS 15', 35990000, 'Dell XPS 15 với Intel Core i7', 2, 70, 1, 'Black', 0, NOW()),
('ASUS ROG Strix', 32990000, 'ASUS ROG Strix gaming laptop', 2, 50, 1, 'Black', 0, NOW()),
('HP Spectre x360', 29990000, 'HP Spectre x360 convertible laptop', 2, 40, 1, 'Silver', 0, NOW()),
('Lenovo ThinkPad X1', 31990000, 'Lenovo ThinkPad X1 Carbon', 2, 65, 1, 'Black', 0, NOW()),
('iPad Pro 12.9"', 25990000, 'iPad Pro với chip M2', 3, 100, 1, 'Space Gray', 0, NOW()),
('Samsung Galaxy Tab S9', 18990000, 'Galaxy Tab S9 với S Pen', 3, 40, 1, 'Black', 0, NOW()),
('iPad Air', 17990000, 'iPad Air với chip M2', 3, 75, 1, 'Blue', 0, NOW()),
('Microsoft Surface Pro 9', 22990000, 'Surface Pro 9 với Windows 11', 3, 55, 1, 'Platinum', 0, NOW()),
('Apple Watch Series 9', 8990000, 'Apple Watch Series 9 với chip S9', 4, 200, 1, 'Midnight', 0, NOW()),
('Samsung Galaxy Watch 6', 6990000, 'Galaxy Watch 6 với Wear OS', 4, 80, 1, 'Graphite', 0, NOW()),
('Google Pixel Watch 2', 7990000, 'Pixel Watch 2 với Fitbit', 4, 60, 1, 'Obsidian', 0, NOW()),
('Garmin Venu 3', 8990000, 'Garmin Venu 3 fitness watch', 4, 50, 1, 'Graphite', 0, NOW());



