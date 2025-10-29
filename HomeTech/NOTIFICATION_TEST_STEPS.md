# 🔔 Notification System Test Steps

## ✅ What We Fixed:

1. **Database Integration**: Notifications now load from the `notifications` table
2. **Unread Count**: Red badge shows number of unread notifications from database
3. **Bell Dropdown**: Clicking bell loads notifications from database in real-time
4. **Auto-Load**: Page automatically loads unread count when you visit the home page

---

## 🧪 How to Test:

### Step 1: Refresh the Home Page
1. Go to `http://localhost:8080/`
2. **Look in the browser console** (F12 → Console tab)
3. You should see:
   ```
   📊 (Inline) Loading initial unread count...
   🔢 (Inline) Updating unread count from DB...
   📊 (Inline) Unread count: 2
   ```

### Step 2: Check the Bell Icon
1. Look at the bell icon in the top-right header
2. **You should see a red badge with "2"** (the number of unread notifications in your database)

### Step 3: Click the Bell
1. Click the bell icon
2. **Console should show:**
   ```
   📥 (Inline) Loading notifications from DB...
   📦 (Inline) Loaded notifications: 2
   ```
3. **The dropdown should show:**
   - "Đơn hàng #19 đã được tạo thành công!" (from database)
   - "Đơn hàng #20 đã được tạo thành công!" (from database)
   - Both should have a cyan highlighted background (unread)

### Step 4: Test New Notification (Place Order)
1. Add a product to cart
2. Click "Đặt hàng" (Place Order)
3. **You should see:**
   - A cyan toast notification pop up: "Đơn hàng #XX đã được tạo thành công!"
   - The bell badge updates to "3"
   - The notification is saved to the database

### Step 5: Verify Database
1. Open phpMyAdmin or MySQL Workbench
2. Run: `SELECT * FROM notifications ORDER BY created_at DESC LIMIT 5;`
3. **You should see all your notifications with:**
   - `is_read = 0` (unread)
   - `type = 'ORDER'`
   - `message = 'Đơn hàng #XX đã được tạo thành công!'`

---

## 🐛 Debugging:

### If the badge doesn't show:
1. Open browser console (F12)
2. Check for these messages:
   - `🔢 (Inline) Updating unread count from DB...`
   - `📊 (Inline) Unread count: X`
3. If count is 0, check the database to confirm you have notifications

### If the dropdown is empty:
1. Click the bell and check console for:
   - `📥 (Inline) Loading notifications from DB...`
   - `📦 (Inline) Loaded notifications: X`
2. Manually test API: Open `http://localhost:8080/api/notifications` in browser
   - Should return JSON array of your notifications

### If you get 403 Forbidden:
1. Check SecurityConfig.java has:
   ```java
   .requestMatchers("/api/notifications/**").permitAll()
   ```
2. Restart Spring Boot

---

## 📊 Current Database State:

Based on your screenshot, you have **2 notifications**:
- ID 1: "Đơn hàng #19 đã được tạo thành công!" (user_id: 4, is_read: 0)
- ID 2: "Đơn hàng #20 đã được tạo thành công!" (user_id: 4, is_read: 0)

**Expected Result:**
- Bell badge should show "2"
- Clicking bell should show both notifications

---

## ✨ Next Steps:

Once this is working, we can add:
- ✅ Mark as read functionality
- ✅ Delete notification button
- ✅ Filter by notification type
- ✅ Notification sound/animation

Let me know what you see in the browser console! 🚀

