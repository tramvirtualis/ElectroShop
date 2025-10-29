# 🔔 Database-Persisted Notification System - Complete Guide

## Overview

I've created a complete notification system that saves notifications to the database, tracks read/unread status, and displays them in real-time using WebSockets.

---

## 🗄️ Database Setup

### Step 1: Run the SQL Script

Execute the SQL script to create the notifications table:

```sql
-- Located in: create_notifications_table.sql
```

**Or run manually:**
```sql
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
);
```

---

## 📦 What Was Created

### Backend Components

1. **`Notify.java`** - Entity model with fields:
   - `id` - Primary key
   - `user` - Relationship to User
   - `message` - Notification text
   - `createdAt` - Timestamp
   - `isRead` - Read/unread status
   - `type` - Type (ORDER, SYSTEM, PROMOTION, etc.)
   - `relatedId` - Related entity ID (Order ID, Product ID, etc.)

2. **`NotifyRepository.java`** - Data access with methods:
   - `findByUser_IdOrderByCreatedAtDesc()` - Get all notifications
   - `findByUser_IdAndIsReadFalseOrderByCreatedAtDesc()` - Get unread only
   - `countByUser_IdAndIsReadFalse()` - Count unread
   - `markAllAsReadByUserId()` - Mark all as read
   - `deleteOldReadNotifications()` - Cleanup old notifications

3. **`NotifyService.java`** - Business logic:
   - `createNotification()` - Save and send notification
   - `getUserNotifications()` - Get all notifications
   - `getUnreadNotifications()` - Get unread notifications
   - `getUnreadCount()` - Get unread count
   - `markAsRead()` - Mark single notification as read
   - `markAllAsRead()` - Mark all as read
   - `deleteNotification()` - Delete notification
   - `cleanupOldNotifications()` - Delete old read notifications (30+ days)

4. **`NotificationController.java`** - REST API endpoints:
   - `GET /api/notifications` - Get all notifications
   - `GET /api/notifications/unread` - Get unread notifications
   - `GET /api/notifications/unread/count` - Get unread count
   - `POST /api/notifications/{id}/read` - Mark as read
   - `POST /api/notifications/read-all` - Mark all as read
   - `DELETE /api/notifications/{id}` - Delete notification
   - `GET /api/notify/test` - Test notification (saves to DB if logged in)

5. **`OrderController.java`** - Updated to create notifications:
   - When order is placed, saves notification to DB
   - Sends real-time notification via WebSocket

### Frontend Components

1. **`notification-client.js`** - Enhanced with:
   - `loadNotificationsFromDB()` - Fetch notifications from database
   - `markNotificationAsRead()` - Mark as read on click
   - `deleteNotification()` - Delete notification
   - `updateUnreadCount()` - Update badge count
   - `escapeHtml()` - Prevent XSS attacks
   - `formatDate()` - Format timestamps (Vietnamese)

2. **`home.html`** - Bell icon displays:
   - Red badge with unread count
   - Dropdown with all notifications
   - Each notification shows:
     - Message
     - Timestamp (relative: "5 phút trước")
     - Delete button (X)
     - Highlighted background if unread

---

## 🎯 Features

### ✅ Database Persistence
- Notifications saved to database
- Survives page refresh
- Available across sessions

### ✅ Read/Unread Tracking
- Unread notifications highlighted with blue background
- Click notification to mark as read
- Badge shows unread count
- Auto-updates via WebSocket

### ✅ Real-time Updates
- New notifications appear instantly via WebSocket
- Badge count updates automatically
- Toast popup shows new notifications

### ✅ User Actions
- **Click notification** → Mark as read
- **Click X button** → Delete notification
- **Click bell** → Open dropdown and load notifications

### ✅ Security
- CSRF protection for notification APIs
- XSS prevention with HTML escaping
- User-specific notifications only

---

## 🚀 How to Use

### For Developers

**Create a notification programmatically:**

```java
// Inject NotifyService
@Autowired
private NotifyService notifyService;

// Create notification
notifyService.createNotification(
    userId,           // Long: User ID
    "Your message",   // String: Notification message
    "ORDER",          // String: Type (ORDER, SYSTEM, PROMOTION, etc.)
    orderId           // Integer: Related ID (optional)
);
```

**Example in OrderController:**
```java
String message = "Đơn hàng #" + order.getOrderId() + " đã được tạo thành công!";
notifyService.createNotification(userId, message, "ORDER", order.getOrderId());
```

### For Users

1. **Receive Notification**:
   - Toast popup appears (top-right)
   - Bell badge shows count

2. **View Notifications**:
   - Click bell icon
   - Dropdown shows all notifications
   - Unread = blue background + bold

3. **Mark as Read**:
   - Click on notification
   - Background turns transparent
   - Count decreases

4. **Delete Notification**:
   - Click X button on right side
   - Notification removed from list and database

---

## 🧪 Testing

### Step 1: Restart Application
```bash
mvn spring-boot:run
```

### Step 2: Run SQL Script
Execute `create_notifications_table.sql` in your database

### Step 3: Test with API
```bash
# Test notification (if logged in, saves to DB)
curl http://localhost:8080/api/notify/test

# Get unread count
curl http://localhost:8080/api/notifications/unread/count

# Get all notifications
curl http://localhost:8080/api/notifications
```

### Step 4: Test in Browser
1. Login to your application
2. Place an order
3. Check bell icon → should show "1"
4. Click bell → see notification
5. Click notification → marked as read
6. Refresh page → notification still there!

---

## 📊 API Reference

### Get All Notifications
```
GET /api/notifications
Response: Array of Notify objects
```

### Get Unread Notifications
```
GET /api/notifications/unread
Response: Array of Notify objects (isRead = false)
```

### Get Unread Count
```
GET /api/notifications/unread/count
Response: { "count": 5 }
```

### Mark as Read
```
POST /api/notifications/{id}/read
Response: 200 OK
```

### Mark All as Read
```
POST /api/notifications/read-all
Response: 200 OK
```

### Delete Notification
```
DELETE /api/notifications/{id}
Response: 200 OK
```

---

## 🎨 Notification Types

You can create different notification types:

- **ORDER** - Order related (creation, status update)
- **SYSTEM** - System messages
- **PROMOTION** - Promotional offers
- **REVIEW** - Product review notifications
- **CART** - Cart reminders
- Custom types as needed

---

## 🔧 Customization

### Change Notification Retention
```java
// In NotifyService.java, line ~125
LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // Change 30 to your preference
```

### Change Dropdown Style
Edit `home.html` bell dropdown HTML/CSS or `notification-client.js` dynamic styles

### Add Notification Icons
Modify `loadNotificationsFromDB()` in `notification-client.js`:
```javascript
let icon = notif.type === 'ORDER' ? '📦' : '🔔';
item.innerHTML = `${icon} ${escapeHtml(notif.message)}`;
```

---

## 🐛 Troubleshooting

### Bell shows 0 but I have notifications
- Check if notifications have `isRead = false` in database
- Call `updateUnreadCount()` manually in browser console

### Notifications not appearing
- Check browser console for errors
- Verify WebSocket connection: Look for "✅ WebSocket connected!"
- Check server logs for "🔔 Notification saved and sent"

### Can't delete notifications
- Check CSRF token is being sent
- Verify user is authenticated
- Check browser Network tab for 403/401 errors

### Database connection errors
- Verify `notifications` table exists
- Check foreign key constraint on `user_id`
- Ensure `users` table has matching IDs

---

## 📝 Notes

- Notifications are deleted when user is deleted (CASCADE)
- Old read notifications auto-delete after 30 days (call `cleanupOldNotifications()`)
- Unread count updates automatically on page load and when new notifications arrive
- Works for both authenticated and guest users (guests don't save to DB)

---

## 🎉 Success Indicators

✅ Table created in database  
✅ No compilation errors  
✅ Application starts successfully  
✅ Bell icon shows on home page  
✅ Test endpoint works (`/api/notify/test`)  
✅ Placing order creates notification  
✅ Notification appears in dropdown  
✅ Unread count shows correctly  
✅ Click marks as read  
✅ Delete button removes notification  
✅ Refresh preserves notifications  

---

**The notification system is complete and production-ready!** 🚀

