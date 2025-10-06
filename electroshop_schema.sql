-- ElectroShop Database Schema
-- Generated based on ERD for e-commerce system

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS electroshop;
USE electroshop;

-- Drop tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS orderitem;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS cartitem;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS shipper;
DROP TABLE IF EXISTS shop;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS account;

-- Create Account table
CREATE TABLE account (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'SHOP_OWNER', 'SHIPPER') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create User table
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Shop table
CREATE TABLE shop (
    id INT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id INT NOT NULL,
    shop_name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Customer table
CREATE TABLE customer (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    birthdate DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Shipper table
CREATE TABLE shipper (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    license_number VARCHAR(50),
    vehicle_type VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Address table
CREATE TABLE address (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    address_line VARCHAR(500) NOT NULL,
    commune VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Category table
CREATE TABLE category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Product table
CREATE TABLE product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shop_id INT NOT NULL,
    category_id INT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    description TEXT,
    product_price DECIMAL(10,2) NOT NULL,
    product_status ENUM('HIDE', 'SHOW') DEFAULT 'SHOW',
    stock_quantity INT DEFAULT 0,
    image_url VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shop_id) REFERENCES shop(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Cart table
CREATE TABLE cart (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    UNIQUE KEY unique_customer_cart (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create CartItem table
CREATE TABLE cartitem (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_product (cart_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Order table
CREATE TABLE `order` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    shipper_id INT,
    order_status ENUM('PENDING', 'CANCELLED', 'SHIPPED', 'DELIVERED') DEFAULT 'PENDING',
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    delivery_date DATETIME,
    total_amount DECIMAL(10,2) NOT NULL,
    shipping_address_id INT NOT NULL,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE RESTRICT,
    FOREIGN KEY (shipper_id) REFERENCES shipper(id) ON DELETE SET NULL,
    FOREIGN KEY (shipping_address_id) REFERENCES address(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create OrderItem table
CREATE TABLE orderitem (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES `order`(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Review table
CREATE TABLE review (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    product_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    review_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    UNIQUE KEY unique_customer_product_review (customer_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Payment table
CREATE TABLE payment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payment_method ENUM('MOMO', 'CARD', 'CASH') NOT NULL,
    payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10,2) NOT NULL,
    transaction_id VARCHAR(255),
    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES `order`(id) ON DELETE CASCADE,
    UNIQUE KEY unique_order_payment (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for better performance
CREATE INDEX idx_user_email ON user(email);
CREATE INDEX idx_user_phone ON user(phone_number);
CREATE INDEX idx_product_shop ON product(shop_id);
CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_status ON product(product_status);
CREATE INDEX idx_order_customer ON `order`(customer_id);
CREATE INDEX idx_order_status ON `order`(order_status);
CREATE INDEX idx_order_date ON `order`(order_date);
CREATE INDEX idx_cartitem_cart ON cartitem(cart_id);
CREATE INDEX idx_cartitem_product ON cartitem(product_id);
CREATE INDEX idx_orderitem_order ON orderitem(order_id);
CREATE INDEX idx_orderitem_product ON orderitem(product_id);
CREATE INDEX idx_review_customer ON review(customer_id);
CREATE INDEX idx_review_product ON review(product_id);
CREATE INDEX idx_payment_order ON payment(order_id);

-- Insert sample data (optional)
-- Sample categories
INSERT INTO category (category_name, description) VALUES
('Electronics', 'Electronic devices and gadgets'),
('Computers', 'Computer hardware and accessories'),
('Mobile Phones', 'Smartphones and mobile accessories'),
('Home Appliances', 'Household electronic appliances');

-- Sample account and user for shop owner
INSERT INTO account (username, password_hash, role) VALUES
('admin', '$2a$10$example_hash_here', 'SHOP_OWNER');

INSERT INTO user (account_id, full_name, phone_number, email) VALUES
(1, 'Admin User', '0123456789', 'admin@electroshop.com');

INSERT INTO shop (owner_user_id, shop_name, description) VALUES
(1, 'ElectroShop Main Store', 'Main electronics store for ElectroShop platform');

-- Sample account and user for customer
INSERT INTO account (username, password_hash, role) VALUES
('customer1', '$2a$10$example_hash_here', 'CUSTOMER');

INSERT INTO user (account_id, full_name, phone_number, email) VALUES
(2, 'John Doe', '0987654321', 'john.doe@email.com');

INSERT INTO customer (user_id, birthdate) VALUES
(2, '1990-01-15');

-- Sample address
INSERT INTO address (customer_id, address_line, commune, city, is_default) VALUES
(1, '123 Main Street, Apartment 4B', 'District 1', 'Ho Chi Minh City', TRUE);

-- Sample products
INSERT INTO product (shop_id, category_id, product_name, description, product_price, product_status, stock_quantity) VALUES
(1, 1, 'Samsung Galaxy S24', 'Latest Samsung smartphone with advanced features', 999.99, 'SHOW', 50),
(1, 2, 'MacBook Pro 14"', 'Apple MacBook Pro with M3 chip', 1999.99, 'SHOW', 25),
(1, 3, 'iPhone 15 Pro', 'Apple iPhone 15 Pro with titanium design', 1199.99, 'SHOW', 30),
(1, 4, 'Samsung Smart TV 55"', '4K UHD Smart TV with Tizen OS', 799.99, 'SHOW', 15);

-- Create cart for customer
INSERT INTO cart (customer_id) VALUES (1);

-- Sample cart items
INSERT INTO cartitem (cart_id, product_id, quantity) VALUES
(1, 1, 1),
(1, 3, 2);

-- Sample order
INSERT INTO `order` (customer_id, order_status, total_amount, shipping_address_id) VALUES
(1, 'PENDING', 3399.97, 1);

-- Sample order items
INSERT INTO orderitem (order_id, product_id, quantity, price) VALUES
(1, 1, 1, 999.99),
(1, 3, 2, 1199.99);

-- Sample payment
INSERT INTO payment (order_id, payment_method, amount, payment_status) VALUES
(1, 'CARD', 3399.97, 'COMPLETED');

-- Sample review
INSERT INTO review (customer_id, product_id, rating, comment) VALUES
(1, 1, 5, 'Excellent phone, very satisfied with the purchase!');

-- Display success message
SELECT 'ElectroShop database schema created successfully!' AS message;

