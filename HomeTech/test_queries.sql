-- Test script to verify database queries work correctly
-- Run these queries in your MySQL database to test

-- 1. Check if products exist
SELECT COUNT(*) as total_products FROM products;

-- 2. Check products with sold_count > 0
SELECT COUNT(*) as products_with_sales FROM products WHERE sold_count > 0;

-- 3. Test the main query (top 10 by sold_count)
SELECT productid, product_name, sold_count, categoryID 
FROM products 
ORDER BY sold_count DESC 
LIMIT 10;

-- 4. Test category-specific query
SELECT p.productid, p.product_name, p.sold_count, c.category_name
FROM products p 
JOIN categories c ON p.categoryID = c.categoryID 
WHERE c.category_name = 'Smartphone'
ORDER BY p.sold_count DESC 
LIMIT 10;

-- 5. Check categories
SELECT * FROM categories;

-- 6. Check if products have proper category relationships
SELECT p.productid, p.product_name, c.category_name, p.sold_count
FROM products p 
LEFT JOIN categories c ON p.categoryID = c.categoryID
ORDER BY p.sold_count DESC
LIMIT 10;




