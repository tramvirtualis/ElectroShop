-- ========================================
-- STEP 1: CREATE NOTIFICATIONS TABLE
-- ========================================
-- Run this SQL in your MySQL database (hometech_db)
-- This is REQUIRED for notifications to work!

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    type VARCHAR(50),
    related_id INT,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- VERIFY TABLE WAS CREATED
-- ========================================
SHOW TABLES LIKE 'notifications';
DESCRIBE notifications;

-- ========================================
-- AFTER RUNNING THIS SQL:
-- 1. Restart your Spring Boot application
-- 2. Login to your website
-- 3. Place an order
-- 4. You should see a notification pop up!
-- ========================================

