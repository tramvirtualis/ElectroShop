# 🔔 Order Status Change Notifications

## Overview
The system now automatically sends notifications to users whenever their order status changes!

## What Was Added

### 1. **OrderService Updates** ✅
Updated `OrderService.java` to send notifications when:
- ✅ Order status is updated (via `updateStatus()`)
- ✅ User cancels their own order (via `cancelOrderByUser()`)
- ✅ Admin cancels an order (via `cancelOrderByAdmin()`)

### 2. **Notification Messages** 📨

The system sends Vietnamese messages based on order status:

| Status | Message |
|--------|---------|
| `WAITING_CONFIRMATION` | Đơn hàng #X đang chờ xác nhận |
| `CONFIRMED` | Đơn hàng #X đã được xác nhận |
| `SHIPPING` | Đơn hàng #X đang được giao |
| `COMPLETED` | Đơn hàng #X đã giao thành công! 🎉 |
| `CANCELLED` (by user) | Đơn hàng #X đã được hủy thành công |
| `CANCELLED` (by admin) | Đơn hàng #X đã bị hủy bởi quản trị viên |

### 3. **How It Works** 🔄

```
Admin/System Changes Order Status
         ↓
OrderService.updateStatus()
         ↓
Saves status to database
         ↓
NotifyService.createNotification()
         ↓
Saves to notifications table
         ↓
Sends real-time WebSocket message
         ↓
User sees notification (bell icon + toast)
```

## Testing

### Test Order Status Changes:

1. **Place an order** (you already know how to do this)
2. **As Admin**, update the order status:
   - Go to Admin Dashboard
   - Find the order
   - Change status from "WAITING_CONFIRMATION" → "CONFIRMED"
   - **Result**: Customer will see notification!

3. **Check the notification**:
   - Look at the bell icon (should show badge)
   - Click the bell to see the dropdown
   - You'll see: "Đơn hàng #X đã được xác nhận"

### API Endpoints That Trigger Notifications:

**Admin (Thymeleaf):**
```
POST /admin/dashboard/orders/update-status
- Parameters: orderId, status
- Triggers: updateStatus() → sends notification
```

**Admin (REST API):**
```
PUT /api/orders/{orderId}/status?newStatus=CONFIRMED
- Triggers: updateStatus() → sends notification
```

**User Cancel:**
```
POST /orders/{id}/cancel
- Triggers: cancelOrderByUser() → sends notification
```

**Admin Cancel:**
```
POST /admin/orders/cancel/{orderId}
- Triggers: cancelOrderByAdmin() → sends notification
```

## Quick Test Steps

### Option 1: Using Admin Dashboard
1. Login as admin
2. Go to Admin Orders page
3. Select an order
4. Change its status
5. User will receive notification immediately!

### Option 2: Using REST API (Postman/curl)
```bash
# Update order status
curl -X PUT "http://localhost:8080/api/orders/22/status?newStatus=SHIPPING" \
  -H "Content-Type: application/json"
```

### Option 3: Simulate in Browser Console
```javascript
// On the orders page, open console and run:
fetch('/api/orders/22/status?newStatus=CONFIRMED', {
  method: 'PUT'
}).then(() => {
  console.log('Status updated! Check your notifications!');
});
```

## Expected Behavior

1. **Immediate Toast**: User sees a sliding toast notification from the right
2. **Bell Badge**: The bell icon shows the unread count (red badge)
3. **Bell Rings**: When hovering over the bell, it animates (rings)
4. **Dropdown**: Click bell to see all unread notifications
5. **Mark as Read**: Click checkmark (✓) button to dismiss
6. **Real-time**: Works even if user is on a different page!

## Features

✅ Real-time notifications via WebSocket  
✅ Database persistence (notifications saved)  
✅ Beautiful animations (bell ring, toast slide)  
✅ Unread count badge  
✅ Mark as read functionality  
✅ Works on both home page and orders page  
✅ Vietnamese language support  
✅ Different notification types (ORDER, ORDER_STATUS, ORDER_CANCELLED)  

## Notification Types

| Type | When Triggered | Example Message |
|------|----------------|-----------------|
| `ORDER` | New order created | Đơn hàng #22 đã được tạo thành công! |
| `ORDER_STATUS` | Status changed | Đơn hàng #22 đã được xác nhận |
| `ORDER_CANCELLED` | Order cancelled | Đơn hàng #22 đã được hủy thành công |

## Database Schema

The `notifications` table stores:
- `id` - Notification ID
- `user_id` - Which user receives it
- `message` - The notification text
- `created_at` - Timestamp
- `is_read` - Read/unread status
- `type` - Notification category (ORDER, ORDER_STATUS, etc.)
- `related_id` - Order ID (for linking)

## Console Output

When a status change happens, you'll see in the server logs:
```
🔔 Notification sent for order #22 status change: CONFIRMED
```

## Summary

🎉 **Your notification system is now complete!**

Users will automatically receive notifications when:
- They place an order
- Admin confirms their order
- Order starts shipping
- Order is delivered
- Order is cancelled (by them or admin)

Everything works in real-time with beautiful animations! 🚀



