# 🔔 Notification System - Complete Guide

## Overview
The notification system is now fully implemented with:
- **Real-time WebSocket notifications** using STOMP/SockJS
- **Red badge on bell icon** showing notification count
- **Dropdown list** showing all notifications when clicking the bell
- **Automatic notifications** when orders are placed

---

## 🧪 How to Test

### Method 1: Test Page (Easiest)
1. Start your application
2. Visit: `http://localhost:8080/test-notification`
3. Click "Send Test Notification" button
4. Go back to home page and check the bell icon - you should see:
   - A red badge with "1" appears on the bell
   - Click the bell to see the notification in the dropdown

### Method 2: Place an Order
1. Add products to cart
2. Go to cart page
3. Click "Đặt hàng" (Place Order)
4. Watch the bell icon on the home page - a red badge should appear
5. Click the bell to see notification: "Bạn có đơn hàng mới! Mã đơn #..."

---

## 🔍 Debugging Steps

If notifications are not showing, check these in order:

### 1. Check Browser Console (F12)
You should see these logs when the page loads:
```
🔌 Connecting to WebSocket...
✅ WebSocket connected!
```

When a notification is sent, you should see:
```
📨 Received notification: {"message":"...","timestamp":"..."}
📢 Showing notification: ...
🔔 Calling window.addNotification
🔔 addNotification called with: ...
🔔 Notification added. New count: 1
```

### 2. Check Server Console
When placing an order or sending test notification:
```
🔔 Sending notification for order: 123
```

### 3. Check Network Tab
- Look for WebSocket connection to `/ws`
- Status should be "101 Switching Protocols" (successful)

---

## 📁 Files Modified

### Backend
1. **OrderController.java**
   - Sends notification when order is placed
   - Uses `SimpMessagingTemplate` to broadcast to `/topic/notifications`

2. **NotificationController.java**
   - Handles WebSocket messages
   - Provides REST endpoint for testing: `/api/notify/test`

3. **WebSocketConfig.java** (should already exist)
   - Configures WebSocket endpoint `/ws`
   - Enables STOMP messaging

### Frontend
1. **layout/base.html**
   - Global WebSocket client connection
   - Listens to `/topic/notifications`
   - Shows toast popup for all pages

2. **home.html**
   - Bell icon with red badge
   - Dropdown notification list
   - `window.addNotification()` function to update bell

---

## 🎨 Notification Bell Features

### Visual Elements
- **Bell Icon**: FontAwesome bell (`fa-bell`)
- **Red Badge**: Shows notification count, hidden when count is 0
- **Dropdown**: Shows list of notifications when bell is clicked
- **Styling**: Matches the futuristic HomeTech theme

### User Interactions
1. **Click bell** → Dropdown opens/closes
2. **Click outside** → Dropdown closes automatically
3. **New notification arrives** → Badge count increases, item appears in dropdown
4. **Toast popup** → Appears for 4 seconds, then fades out

---

## 🚀 Next Steps

If it's still not working after testing:

1. **Restart the application completely**
   - Stop the server
   - Run `mvn clean install`
   - Start again

2. **Check dependencies in pom.xml**
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-websocket</artifactId>
   </dependency>
   <dependency>
       <groupId>org.springframework</groupId>
       <artifactId>spring-websocket</artifactId>
   </dependency>
   ```

3. **Check browser compatibility**
   - Use Chrome, Edge, or Firefox (latest versions)
   - Clear browser cache and hard refresh (Ctrl + Shift + R)

4. **Check firewall/antivirus**
   - Some security software may block WebSocket connections
   - Try disabling temporarily for testing

---

## 📝 Technical Details

### Message Flow
```
Order Placed 
  → OrderController.createOrder()
  → messagingTemplate.convertAndSend("/topic/notifications", notification)
  → WebSocket broadcasts to all connected clients
  → base.html receives message
  → Calls window.addNotification(msg)
  → Updates bell badge and dropdown
  → Shows toast popup
```

### Notification Format
```json
{
  "message": "Bạn có đơn hàng mới! Mã đơn #123",
  "timestamp": "2025-10-29T21:30:00"
}
```

---

## 🎯 Expected Behavior

### When Order is Placed:
1. ✅ Toast popup appears in top-right: "Bạn có đơn hàng mới! Mã đơn #..."
2. ✅ Bell badge shows red "1" (or increments existing count)
3. ✅ Clicking bell shows notification in dropdown
4. ✅ Notification persists in dropdown until page refresh

### When Test Notification is Sent:
1. ✅ Same behavior as order notification
2. ✅ Message: "Đây là thông báo thử nghiệm!"

---

## ❓ Troubleshooting

**Problem**: No WebSocket connection in browser console  
**Solution**: Check WebSocketConfig.java exists, restart server

**Problem**: "WebSocket connection error" in console  
**Solution**: Check server is running on port 8080, no other service blocking

**Problem**: Bell doesn't show badge  
**Solution**: Check console for `addNotification` calls, inspect element for `notifyBadge`

**Problem**: Notifications work on test page but not on home  
**Solution**: Make sure you're on the home page when notification is sent (WebSocket is page-specific)

---

## 📞 Support

If you continue to have issues:
1. Share the browser console output (all logs starting with 🔌, 📨, 🔔)
2. Share the server console output (logs with 🔔)
3. Screenshot of the Network tab showing WebSocket connection status

