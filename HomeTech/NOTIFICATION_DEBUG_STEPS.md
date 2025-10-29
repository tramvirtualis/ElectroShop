# 🔧 Notification System - Debug Steps

## Quick Test Instructions

### Step 1: Restart the Application
1. **Stop** the Spring Boot application completely
2. Run: `mvn clean install` (or just restart if using IDE)
3. **Start** the application again
4. Wait until you see: `Started HomeTechApplication` in console

### Step 2: Test WebSocket Connection
1. Open browser and go to: `http://localhost:8080/`
2. Open **Developer Tools** (press F12)
3. Go to **Console** tab
4. You should see:
   ```
   🔌 Connecting to WebSocket...
   ✅ WebSocket connected!
   ```
5. If you DON'T see this, there's a WebSocket connection problem

### Step 3: Test Notification System (Easy Way)
1. Keep the home page open with F12 console visible
2. In a **new tab**, go to: `http://localhost:8080/test-notification`
3. Click **"Send Test Notification"** button
4. Go back to the **home page tab**
5. Check the bell icon (top right):
   - Should show a red badge with "1"
   - Click bell to see the notification in dropdown

### Step 4: Check Console Logs
In the browser console, you should see:
```
📨 Received notification: {"message":"Đây là thông báo thử nghiệm!","timestamp":"..."}
📢 Showing notification: Đây là thông báo thử nghiệm!
🔔 Calling window.addNotification
🔔 addNotification called with: Đây là thông báo thử nghiệm!
🔔 Notification added. New count: 1
```

In the server console, you should see:
```
🔔 Sending GET test notification: Đây là thông báo thử nghiệm!
```

---

## If Test Notification Works but Order Notification Doesn't

### Step 5: Test Order Placement
1. Add a product to cart
2. Go to cart page
3. Click **"Đặt hàng"**
4. Check **server console** for these logs:
   ```
   🛒 createOrder called
   👤 Creating order for authenticated user: ...
   ✅ Order created successfully. Order ID: ...
   🔔 Attempting to send notification: Bạn có đơn hàng mới! Mã đơn #...
   ✅ Notification sent successfully!
   ```
5. If you see **"❌ Failed to send notification"**, copy the error and share it

---

## Common Issues & Solutions

### Issue 1: No WebSocket Connection
**Symptoms:**
- Console doesn't show "🔌 Connecting to WebSocket..."
- Or shows "❌ WebSocket connection error"

**Solutions:**
1. Check if `layout/base.html` is being loaded (view page source)
2. Check browser network tab for `/ws` connection (should be "101 Switching Protocols")
3. Try a different browser (Chrome/Edge recommended)
4. Check firewall/antivirus isn't blocking WebSocket

### Issue 2: WebSocket Connects but No Notifications
**Symptoms:**
- Console shows "✅ WebSocket connected!"
- But clicking test button doesn't show notification

**Solutions:**
1. Check server console for the 🔔 send log
2. If not there, notification isn't being sent from backend
3. Check `SimpMessagingTemplate` bean is injected properly
4. Restart application completely

### Issue 3: Bell Icon Not Visible
**Symptoms:**
- Can't find bell icon on page

**Solutions:**
1. Make sure you're on the **home page** (`/`)
2. Bell is in the top-right header area
3. Try refreshing the page (Ctrl + F5)
4. Check if `home.html` is being loaded (not `layout/base.html`)

### Issue 4: Notification Received but Badge Doesn't Show
**Symptoms:**
- Console shows all 🔔 logs
- But bell badge stays hidden

**Solutions:**
1. Check if `window.addNotification` is defined:
   ```javascript
   // Type this in browser console:
   typeof window.addNotification
   // Should return: "function"
   ```
2. Inspect element on bell icon, look for `id="notifyBadge"`
3. Check if it has class `d-none` (if yes, the count isn't updating)

---

## Manual Testing in Console

### Test 1: Check if bell elements exist
```javascript
document.getElementById('notifyBell')     // Should return: button element
document.getElementById('notifyBadge')    // Should return: span element
document.getElementById('notificationsList')  // Should return: div element
```

### Test 2: Manually trigger notification
```javascript
window.addNotification("Test manual notification")
```
Badge should increase and notification should appear in dropdown.

### Test 3: Check WebSocket state
Open browser console and type:
```javascript
// No direct way, but check if connection logs appeared
```

---

## What to Share if Still Not Working

Please provide:
1. **Browser console output** (entire log from page load)
2. **Server console output** (from app start to placing order)
3. **Network tab screenshot** showing `/ws` connection status
4. **Screenshot** of the home page showing (or not showing) the bell icon

Copy all text starting with emoji symbols like:
- 🔌 🔔 📨 📢 ✅ ❌ 🛒 👤 👥

---

## Expected Full Flow

### When Everything Works:
1. User opens home page → WebSocket connects
2. User places order → Server sends notification
3. WebSocket delivers to all connected clients
4. Browser receives message → Parses JSON
5. Calls `window.addNotification()` → Updates bell badge
6. Shows toast popup for 4 seconds
7. Clicking bell shows notification in dropdown

### Logs You Should See:

**Browser Console:**
```
🔌 Connecting to WebSocket...
✅ WebSocket connected!
📨 Received notification: {"message":"...","timestamp":"..."}
📢 Showing notification: ...
🔔 Calling window.addNotification
🔔 addNotification called with: ...
🔔 Notification added. New count: 1
```

**Server Console:**
```
🛒 createOrder called
👤 Creating order for authenticated user: 4
✅ Order created successfully. Order ID: 123
🔔 Attempting to send notification: Bạn có đơn hàng mới! Mã đơn #123
✅ Notification sent successfully!
```

---

## Quick Checklist

- [ ] Application restarted after changes
- [ ] Browser shows "✅ WebSocket connected!" in console
- [ ] Test notification page works (`/test-notification`)
- [ ] Bell icon is visible on home page
- [ ] Server logs show 🔔 when notification sent
- [ ] Browser logs show 📨 when notification received
- [ ] Red badge appears on bell
- [ ] Clicking bell shows notification in dropdown
- [ ] Toast popup appears in top-right corner

