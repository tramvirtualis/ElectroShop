# 🔔 Manual Database Notification Trigger Guide

## Problem
When you manually update order status **directly in the database** (using SQL), the WebSocket notification doesn't trigger because you're bypassing the Java application code.

## Solution
I've created a **manual trigger API endpoint** that you can call after updating the database!

## The API Endpoint

```
POST /api/notify/order-status-manual
Content-Type: application/json

Body:
{
  "orderId": 22,
  "userId": 4,
  "status": "CONFIRMED"
}
```

## How to Use

### Method 1: Using Postman or Thunder Client

1. **Update the database manually** (SQL):
```sql
UPDATE orders SET order_status = 'CONFIRMED' WHERE order_id = 22;
```

2. **Call the API** to trigger the notification:
```
POST http://localhost:8080/api/notify/order-status-manual
Content-Type: application/json

{
  "orderId": 22,
  "userId": 4,
  "status": "CONFIRMED"
}
```

3. **User receives notification** via WebSocket! 🎉

### Method 2: Using curl (Command Line)

```bash
# Step 1: Update database
# (run your SQL query)

# Step 2: Trigger notification
curl -X POST http://localhost:8080/api/notify/order-status-manual \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 22,
    "userId": 4,
    "status": "CONFIRMED"
  }'
```

### Method 3: Using Browser Console

```javascript
// After manually updating the database, run this in browser console:
fetch('/api/notify/order-status-manual', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    orderId: 22,
    userId: 4,
    status: 'CONFIRMED'
  })
}).then(r => r.text()).then(console.log);
```

### Method 4: Create an HTML Test Page

I can create a simple HTML page with a form to trigger notifications easily!

## Supported Status Values

| Status | Notification Message |
|--------|---------------------|
| `WAITING_CONFIRMATION` | đang chờ xác nhận |
| `CONFIRMED` | đã được xác nhận |
| `SHIPPING` | đang được giao |
| `COMPLETED` | đã giao thành công! 🎉 |
| `CANCELLED` | đã bị hủy |

## How to Find User ID

To find the user ID for an order, run this SQL:

```sql
SELECT 
    o.order_id,
    c.customer_id,
    u.id as user_id,
    u.email,
    o.order_status
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
JOIN users u ON c.user_id = u.id
WHERE o.order_id = 22;
```

## Complete Workflow Example

### Scenario: You want to mark order #22 as "SHIPPING"

**Step 1: Find the user ID**
```sql
SELECT c.user_id 
FROM orders o 
JOIN customers c ON o.customer_id = c.customer_id 
WHERE o.order_id = 22;
-- Result: user_id = 4
```

**Step 2: Update the order status in database**
```sql
UPDATE orders 
SET order_status = 'SHIPPING' 
WHERE order_id = 22;
```

**Step 3: Trigger the notification**
```bash
curl -X POST http://localhost:8080/api/notify/order-status-manual \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 22,
    "userId": 4,
    "status": "SHIPPING"
  }'
```

**Result:** User #4 immediately sees:
- 🔔 Toast notification: "Đơn hàng #22 đang được giao"
- Bell badge updates
- Notification appears in dropdown

## Why This is Needed

```
Normal Flow (Automatic):
Admin UI → OrderService.updateStatus() → Database + WebSocket ✅

Manual Database Update (Your Case):
SQL → Database ❌ (WebSocket not triggered)
SQL → Database → Manual API Call → WebSocket ✅
```

## Better Approach (Recommended)

Instead of manually updating the database, use the **REST API** to update order status:

```bash
# This automatically triggers both database update AND notification
curl -X PUT "http://localhost:8080/api/orders/22/status?newStatus=CONFIRMED" \
  -H "Content-Type: application/json"
```

This way you don't need two separate calls!

## Available REST API Endpoints

### Update Order Status (Admin)
```
PUT /api/orders/{orderId}/status?newStatus={status}

Example:
PUT /api/orders/22/status?newStatus=SHIPPING
```

### Cancel Order (Admin)
```
PUT /api/orders/{orderId}/cancel/admin

Example:
PUT /api/orders/22/cancel/admin
```

### Cancel Order (User)
```
PUT /api/orders/{orderId}/cancel/user/{userId}

Example:
PUT /api/orders/22/cancel/user/4
```

All of these **automatically** trigger notifications! 🎉

## Summary

✅ **WebSocket is already working!**  
✅ Use `/api/notify/order-status-manual` if you update database manually  
✅ Better: Use `/api/orders/{id}/status` API to update (automatic notification)  
✅ Admin UI also works (automatic notification)  

Choose your preferred method based on your workflow! 🚀



