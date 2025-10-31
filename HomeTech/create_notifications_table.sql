-- Create notifications table for database persistence
-- This table stores all user notifications with read/unread status

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

-- Sample test data (optional - remove in production)
-- INSERT INTO notifications (user_id, message, type, related_id, is_read)
-- VALUES 
--   (1, 'Đơn hàng #123 đã được tạo thành công!', 'ORDER', 123, FALSE),
--   (1, 'Chào mừng bạn đến với HomeTech!', 'SYSTEM', NULL, FALSE);



