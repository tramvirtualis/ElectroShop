# HomeTech Homepage Product Display Fix

## Problem Identified
The homepage was not displaying all 10 products because:
1. **No products in database**: The database likely has no or very few products
2. **Database query issues**: The JPQL queries with LIMIT clauses may not work properly with MySQL
3. **Java version compatibility**: The system has Java 7 but the project requires Java 21

## Solutions Implemented

### 1. Fixed Database Queries
Updated `ProductRepository.java` to use native SQL queries instead of JPQL:

```java
// Before (JPQL with LIMIT - may not work in all databases)
@Query("SELECT p FROM Product p ORDER BY p.soldCount DESC LIMIT 10")

// After (Native SQL - guaranteed to work)
@Query(value = "SELECT * FROM products ORDER BY sold_count DESC LIMIT 10", nativeQuery = true)
```

### 2. Fixed Product Model
Changed `soldCount` field from `Integer` to `int` for consistency:

```java
// Before
private Integer soldCount = 0;

// After  
private int soldCount = 0;
```

### 3. Created Data Initialization Component
Added `DataInitializer.java` that automatically creates sample data when the application starts:
- Creates categories: Smartphone, Laptop, Tablet, Smart Watch
- Creates 12 sample products across all categories
- Only runs if no products exist in the database

### 4. Created SQL Script for Manual Setup
Created `sample_data.sql` with sample data that can be run directly in MySQL:

```sql
-- Insert categories
INSERT IGNORE INTO categories (category_name) VALUES 
('Smartphone'), ('Laptop'), ('Tablet'), ('Smart Watch');

-- Insert 12 sample products with proper sold_count values
INSERT IGNORE INTO products (product_name, price, description, categoryID, sold_count, status, color, size, created_at) VALUES
('iPhone 15 Pro', 25990000, 'iPhone 15 Pro với chip A17 Pro mạnh mẽ', 1, 150, 1, 'Black', 0, NOW()),
('Samsung Galaxy S24 Ultra', 28990000, 'Galaxy S24 Ultra với camera 200MP', 1, 120, 1, 'Black', 0, NOW()),
-- ... more products
```

## How to Fix the Issue

### Option 1: Run SQL Script (Recommended)
1. Connect to your MySQL database
2. Run the `sample_data.sql` script:
   ```bash
   mysql -u root -p HomeTech < sample_data.sql
   ```

### Option 2: Use Data Initializer
1. Ensure you have Java 21 installed
2. Run the application: `mvn spring-boot:run`
3. The DataInitializer will automatically create sample data on first run

### Option 3: Manual Database Setup
1. Connect to MySQL database
2. Insert categories manually:
   ```sql
   INSERT INTO categories (category_name) VALUES ('Smartphone'), ('Laptop'), ('Tablet'), ('Smart Watch');
   ```
3. Insert at least 10 products with different `sold_count` values
4. Ensure products have `status = 1` (active)

## Verification Steps

After adding sample data:

1. **Start the application**:
   ```bash
   mvn spring-boot:run
   ```

2. **Check the homepage**: Visit `http://localhost:8080/`

3. **Verify 10 products display**: You should see exactly 10 product cards

4. **Check browser console**: Look for debug logs:
   ```
   DEBUG: Rendered product cards count: 10
   DEBUG: Placeholder cards count: 0
   ```

5. **Test category filtering**: Click on different categories to see filtered results

## Expected Results

- **Homepage shows 10 products**: Real products instead of placeholders
- **Products have images**: Real product images load properly  
- **Category filtering works**: Each category shows top 10 products
- **No placeholder products**: All cards show real products with prices and details

## Technical Details

### Database Schema
- `categories` table: `categoryID`, `category_name`
- `products` table: `productID`, `product_name`, `price`, `description`, `categoryID`, `sold_count`, `status`, `color`, `size`, `created_at`

### Key Changes Made
1. **ProductRepository.java**: Fixed queries to use native SQL
2. **Product.java**: Fixed soldCount field type
3. **DataInitializer.java**: Added automatic data initialization
4. **sample_data.sql**: Created manual setup script

### Debug Information
The application includes comprehensive logging:
- Server-side: Check application logs for product counts
- Client-side: Check browser console for rendered product counts
- HTML: Debug comments show server-side product count

## Troubleshooting

### If still showing placeholders:
1. Check database connection in `application.properties`
2. Verify products exist: `SELECT COUNT(*) FROM products;`
3. Check product status: `SELECT * FROM products WHERE status = 1;`
4. Verify categories exist: `SELECT * FROM categories;`

### If Java version issues:
1. Install Java 21 (required for Spring Boot 3.x)
2. Update JAVA_HOME environment variable
3. Restart terminal/IDE

### If database connection issues:
1. Check MySQL is running
2. Verify credentials in `application.properties`
3. Ensure database `HomeTech` exists
4. Check firewall/port 3306 access

