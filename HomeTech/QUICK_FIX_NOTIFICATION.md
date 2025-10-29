# 🔧 Quick Fix for Notifications Not Working

## Problem
You placed an order but no notification appeared in the bell dropdown.

## Root Causes & Solutions

### ✅ Solution 1: Create the Database Table

**The notifications table doesn't exist yet!** You need to create it first.

#### Option A: Using MySQL Workbench or phpMyAdmin
1. Open your database tool
2. Select your HomeTech database
3. Copy and run this SQL:

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### Option B: Using Command Line
```bash
mysql -u your_username -p your_database_name < create_notifications_table.sql
```

---

### ✅ Solution 2: Verify JavaScript is Loading

Open browser console (F12) and check for these logs:
- `📦 notification-client.js loaded`
- `🔌 Connecting to WebSocket...`
- `✅ WebSocket connected!`

If you DON'T see these:
1. Hard refresh: `Ctrl + Shift + R`
2. Check `/js/notification-client.js` file exists
3. Check browser Network tab for 404 errors

---

### ✅ Solution 3: Check Server Logs

When you place an order, check the server console for:
```
🛒 createOrder called
👤 Creating order for authenticated user: X
✅ Order created successfully. Order ID: X
🔔 Notification saved and sent to user X
```

If you see:
- `❌ Failed to send notification` → Check the error message
- Nothing at all → OrderController not executing properly

---

### ✅ Solution 4: Test with Simple API Call

Open browser console and run:
```javascript
fetch('/api/notify/test').then(r => r.text()).then(console.log)
```

**Expected output:** `"Notification saved and sent!"`

Then check the bell - count should increase.

---

## 🎯 Step-by-Step Fix Process

### Step 1: Create Table
Run the SQL script above ☝️

### Step 2: Restart Application
Stop and restart your Spring Boot app

### Step 3: Login
Make sure you're logged in with Google or regular account

### Step 4: Test
Run in browser console:
```javascript
fetch('/api/notify/test').then(r => r.text()).then(console.log)
```

### Step 5: Check Bell
- Bell should show red badge "1"
- Click bell → See "Đây là thông báo thử nghiệm!"

### Step 6: Place Order
- Add product to cart
- Place order
- Bell count should increase
- Click bell → See order notification

---

## 🔍 Debugging Checklist

- [ ] Database table `notifications` exists
- [ ] Application restarted after creating table
- [ ] Logged in as a user (not guest)
- [ ] Browser console shows "📦 notification-client.js loaded"
- [ ] Browser console shows "✅ WebSocket connected!"
- [ ] Test endpoint works: `/api/notify/test`
- [ ] Server logs show "🔔 Notification saved and sent"
- [ ] Bell icon visible on page
- [ ] No JavaScript errors in console

---

## 🚨 Common Issues

### Issue 1: "Failed to send notification"
**Solution:** Check if `NotifyService` bean is injected properly. Restart application.

### Issue 2: Bell shows 0 even after test
**Solution:** 
1. Open browser console
2. Run: `updateUnreadCount()`
3. Or refresh page

### Issue 3: "user_id constraint failed"
**Solution:** Your user ID doesn't exist in `users` table. Check:
```sql
SELECT id FROM users WHERE email = 'your_email@example.com';
```

### Issue 4: JavaScript console shows "401 Unauthorized"
**Solution:** 
1. You're not logged in
2. Login first, then test again

---

## 💡 Quick Test Script

Paste this in browser console to diagnose:
```javascript
(async function testNotifications() {
    console.log('🔧 Testing notification system...');
    
    // Test 1: Check JS loaded
    console.log('Test 1: notification-client.js loaded?', typeof updateUnreadCount === 'function');
    
    // Test 2: Check WebSocket
    console.log('Test 2: Checking WebSocket connection...');
    
    // Test 3: Test API
    console.log('Test 3: Testing API...');
    const response = await fetch('/api/notify/test');
    const result = await response.text();
    console.log('API Response:', result);
    
    // Test 4: Check count
    console.log('Test 4: Getting unread count...');
    const countResponse = await fetch('/api/notifications/unread/count');
    const countData = await countResponse.json();
    console.log('Unread count:', countData.count);
    
    // Test 5: Get all notifications
    console.log('Test 5: Getting all notifications...');
    const notifsResponse = await fetch('/api/notifications');
    const notifs = await notifsResponse.json();
    console.log('Total notifications:', notifs.length);
    notifs.forEach(n => console.log('-', n.message, '(read:', n.read + ')'));
    
    console.log('✅ Test complete! Check results above.');
})();
```

---

## 📞 Still Not Working?

Share these details:
1. Browser console output (all logs)
2. Server console output (when placing order)
3. Result of running the test script above
4. Screenshot of bell area

Most likely issue: **Table not created yet** ← Do this first!

