# 🔔 WebSocket Notification System - Complete Guide

## ✅ Yes, WebSocket is Fully Implemented!

Your notification system **DOES use WebSocket**! Here's how it works:

## How the WebSocket System Works

```
┌─────────────────────────────────────────────────────────────┐
│  Order Status Change (3 Ways)                               │
├─────────────────────────────────────────────────────────────┤
│  1. Admin UI (Dashboard)                                    │
│  2. REST API Call                                           │
│  3. Manual Database Update + Trigger API                    │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  OrderService.updateStatus()                                │
│  - Saves status to database                                 │
│  - Calls NotifyService.createNotification()                 │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  NotifyService                                              │
│  1. Saves notification to database (notifications table)   │
│  2. Broadcasts via WebSocket (SimpMessagingTemplate)       │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  WebSocket Channels                                         │
│  - /topic/notifications (all users)                         │
│  - /topic/notifications/{userId} (specific user)            │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  User's Browser (JavaScript)                                │
│  1. SockJS + Stomp.js connects to /ws                       │
│  2. Subscribes to /topic/notifications                      │
│  3. Receives real-time messages                             │
│  4. Shows toast notification                                │
│  5. Updates bell badge                                      │
│  6. Animates bell icon                                      │
└─────────────────────────────────────────────────────────────┘
```

## Why Didn't It Work for You?

**When you update the database directly (SQL):**
```sql
UPDATE orders SET order_status = 'SHIPPING' WHERE order_id = 22;
```

❌ This **bypasses** the Java code  
❌ `OrderService.updateStatus()` is **not called**  
❌ WebSocket notification is **not triggered**

**Solution:** Use one of these methods instead:

## Method 1: Use Admin UI (Easiest) ✅

1. Login as admin
2. Go to Admin Dashboard → Orders
3. Click on an order
4. Change the status
5. **WebSocket notification sent automatically!** 🎉

## Method 2: Use REST API ✅

```bash
# This calls OrderService.updateStatus() → triggers WebSocket
curl -X PUT "http://localhost:8080/api/orders/22/status?newStatus=CONFIRMED"
```

## Method 3: Manual Database + Trigger API ✅

**Step 1:** Update database manually
```sql
UPDATE orders SET order_status = 'SHIPPING' WHERE order_id = 22;
```

**Step 2:** Trigger the notification
```bash
curl -X POST http://localhost:8080/api/notify/order-status-manual \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 22,
    "userId": 4,
    "status": "SHIPPING"
  }'
```

## Method 4: Use Test Page (Easiest for Testing) ✅

**I created a beautiful test page for you!**

Visit: http://localhost:8080/test-notification-trigger.html

Features:
- 🎨 Beautiful UI matching your theme
- 📋 Simple form (Order ID, User ID, Status)
- ⚡ Quick test buttons (one-click testing)
- ✅ Shows success/error messages
- 🔔 Instantly triggers WebSocket notifications

## WebSocket Configuration

### Backend (Spring Boot)

**WebSocketConfig.java:**
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
```

**NotifyService.java:**
```java
private void sendRealtimeNotification(Long userId, Notify notification) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("id", notification.getId());
    payload.put("message", notification.getMessage());
    // ... other fields ...
    
    // Broadcast via WebSocket
    messagingTemplate.convertAndSend("/topic/notifications/" + userId, payload);
    messagingTemplate.convertAndSend("/topic/notifications", payload);
}
```

### Frontend (JavaScript)

**home.html & orders/index.html:**
```javascript
// Connect to WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('✅ WebSocket connected!');
    
    // Subscribe to notifications
    stompClient.subscribe('/topic/notifications', function(message) {
        const notification = JSON.parse(message.body);
        showToast(notification.message);
        updateBadgeCount();
    });
});
```

## Testing the WebSocket Connection

### 1. Check Browser Console
Open DevTools (F12) → Console

You should see:
```
🚀 Initializing notification system...
✅ WebSocket connected!
```

### 2. Check Network Tab
DevTools → Network → WS (WebSocket)

You should see:
- Connection to `ws://localhost:8080/ws`
- Status: **Connected** (green)

### 3. Test Notification
Visit: http://localhost:8080/api/notify/test

You should see:
- Toast notification appears
- Bell badge updates
- Console logs the notification

## Troubleshooting

### Problem: "WebSocket not connecting"
**Solution:**
```javascript
// Check in browser console:
console.log(typeof SockJS);  // should be "function"
console.log(typeof Stomp);   // should be "object"
```

If undefined, the libraries aren't loaded. Check:
```html
<script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js"></script>
```

### Problem: "Manual database update doesn't trigger notification"
**Solution:** That's expected! Use the trigger API:
```bash
POST /api/notify/order-status-manual
```

### Problem: "Notification appears but bell doesn't update"
**Solution:** The `updateUnreadCount()` function needs to be called:
```javascript
async function updateUnreadCount() {
    const response = await fetch('/api/notifications/unread/count');
    const data = await response.json();
    updateBadgeCount(data.count);
}
```

## Complete Testing Workflow

### Scenario: Test Order #22 Status Change

**Step 1:** Find the user ID
```sql
SELECT c.user_id, o.order_status 
FROM orders o 
JOIN customers c ON o.customer_id = c.customer_id 
WHERE o.order_id = 22;
-- Result: user_id = 4, order_status = WAITING_CONFIRMATION
```

**Step 2:** Open the user's page in browser
- Login as the user
- Go to homepage or orders page
- Keep the page open

**Step 3:** Trigger status change (choose one method)

**Option A:** Use Test Page
1. Open http://localhost:8080/test-notification-trigger.html
2. Enter: Order ID: 22, User ID: 4, Status: CONFIRMED
3. Click "Send Notification"

**Option B:** Use REST API
```bash
curl -X PUT "http://localhost:8080/api/orders/22/status?newStatus=CONFIRMED"
```

**Option C:** Use Admin UI
1. Login as admin
2. Go to orders
3. Update order #22 status to CONFIRMED

**Step 4:** Watch the magic! 🎉
- Toast notification slides in: "Đơn hàng #22 đã được xác nhận"
- Bell badge shows "1"
- Bell animates on hover
- Click bell → see notification in dropdown

## Summary

✅ **WebSocket IS implemented and working!**  
✅ Use Admin UI or REST API for automatic notifications  
✅ Use `/api/notify/order-status-manual` for manual database updates  
✅ Use the test page for easy testing: http://localhost:8080/test-notification-trigger.html  
✅ Real-time, animated, beautiful notifications! 🚀

The system works perfectly when you use the proper channels (Admin UI or REST API). If you update the database directly, just call the trigger API afterward!

