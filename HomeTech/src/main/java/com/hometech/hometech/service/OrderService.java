package com.hometech.hometech.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hometech.hometech.Repository.CartItemRepository;
import com.hometech.hometech.Repository.CustomerRepository;
import com.hometech.hometech.Repository.OrderItemRepository;
import com.hometech.hometech.Repository.OrderRepository;
import com.hometech.hometech.enums.OrderStatus;
import com.hometech.hometech.model.CartItem;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.Order;
import com.hometech.hometech.model.OrderItem;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final CartItemRepository cartRepo;
    private final CustomerRepository customerRepo;
    private final NotifyService notifyService;

    public OrderService(OrderRepository orderRepo, OrderItemRepository orderItemRepo,
                        CartItemRepository cartRepo, CustomerRepository customerRepo,
                        NotifyService notifyService) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.cartRepo = cartRepo;
        this.customerRepo = customerRepo;
        this.notifyService = notifyService;
    }

    // 🟢 Tạo đơn hàng từ giỏ hàng của user cụ thể
    public Order createOrder(Long userId) {
        Customer customer = customerRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (customer.getCart() == null) {
            throw new RuntimeException("Customer cart not found");
        }
        
        List<CartItem> cartItems = cartRepo.findByCart(customer.getCart());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống!");
        }

        double total = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        // Tính tổng tiền
        for (CartItem c : cartItems) {
            double subtotal = c.getProduct().getPrice() * c.getQuantity();
            total += subtotal;

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(c.getProduct());
            orderItem.setQuantity(c.getQuantity());
            orderItem.setPrice(c.getProduct().getPrice());
            orderItems.add(orderItem);
        }

        // Tạo đơn hàng
        Order order = new Order();
        order.setCustomer(customer);
        order.setTotalPrice(total);
        order.setOrderStatus(OrderStatus.WAITING_CONFIRMATION);
        order.setOrderItems(orderItems);
        order.setOrderDate(new java.util.Date());

        // Liên kết ngược
        orderItems.forEach(item -> item.setOrder(order));

        // Lưu đơn hàng và chi tiết
        orderRepo.save(order);
        orderItemRepo.saveAll(orderItems);

        // Xóa giỏ hàng sau khi đặt hàng
        cartRepo.deleteAll(cartItems);

        return order;
    }

    public Order createOrder(Long userId, String shippingAddress) {
        Order order = createOrder(userId);
        if (shippingAddress != null && !shippingAddress.isBlank()) {
            order.setShippingAddress(shippingAddress);
            orderRepo.save(order);
        }
        return order;
    }

    // 🆕 Tạo đơn hàng cho khách (guest) theo session guest_{sessionId}@guest.local
    public Order createOrderForSession(String sessionId) {
        String guestEmail = "guest_" + sessionId + "@guest.local";
        com.hometech.hometech.model.User guestUser = customerRepo
                .findAll()
                .stream()
                .map(Customer::getUser)
                .filter(u -> guestEmail.equals(u.getEmail()))
                .findFirst()
                .orElse(null);
        if (guestUser == null) {
            throw new RuntimeException("No guest cart for this session");
        }
        Customer customer = customerRepo.findByUser_Id(guestUser.getId())
                .orElseThrow(() -> new RuntimeException("Guest customer not found"));
        if (customer.getCart() == null) throw new RuntimeException("Guest cart not found");

        List<CartItem> cartItems = cartRepo.findByCart(customer.getCart());
        if (cartItems.isEmpty()) throw new RuntimeException("Giỏ hàng trống!");

        double total = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem c : cartItems) {
            total += c.getProduct().getPrice() * c.getQuantity();
            OrderItem oi = new OrderItem();
            oi.setProduct(c.getProduct());
            oi.setQuantity(c.getQuantity());
            oi.setPrice(c.getProduct().getPrice());
            orderItems.add(oi);
        }
        Order order = new Order();
        order.setCustomer(customer);
        order.setTotalPrice(total);
        order.setOrderStatus(OrderStatus.WAITING_CONFIRMATION);
        order.setOrderItems(orderItems);
        order.setOrderDate(new java.util.Date());
        orderItems.forEach(i -> i.setOrder(order));
        orderRepo.save(order);
        orderItemRepo.saveAll(orderItems);
        cartRepo.deleteAll(cartItems);
        return order;
    }

    public Order createOrderForSession(String sessionId, String shippingAddress) {
        Order order = createOrderForSession(sessionId);
        if (shippingAddress != null && !shippingAddress.isBlank()) {
            order.setShippingAddress(shippingAddress);
            orderRepo.save(order);
        }
        return order;
    }

    // 🟡 Cập nhật trạng thái đơn hàng
    public Order updateStatus(int orderId, OrderStatus newStatus) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));
        
        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);
        Order savedOrder = orderRepo.save(order);
        
        // 🔔 Send notification to customer when status changes
        if (oldStatus != newStatus && order.getCustomer() != null && order.getCustomer().getUser() != null) {
            try {
                Long userId = order.getCustomer().getUser().getId();
                String statusMessage = getStatusMessage(newStatus);
                String message = String.format("Đơn hàng #%d %s", orderId, statusMessage);
                notifyService.createNotification(userId, message, "ORDER_STATUS", orderId);
                System.out.println("🔔 Notification sent for order #" + orderId + " status change: " + newStatus);
            } catch (Exception e) {
                System.err.println("❌ Failed to send notification for order status change: " + e.getMessage());
            }
        }
        
        return savedOrder;
    }
    
    // Helper method to get Vietnamese status message
    private String getStatusMessage(OrderStatus status) {
        switch (status) {
            case WAITING_CONFIRMATION:
                return "đang chờ xác nhận";
            case CONFIRMED:
                return "đã được xác nhận";
            case SHIPPED:
                return "đang được giao";
            case COMPLETED:
                return "đã giao thành công! 🎉";
            case CANCELLED:
                return "đã bị hủy";
            default:
                return "đã thay đổi trạng thái";
        }
    }

    // 🔵 Lấy danh sách đơn hàng (deprecated - chỉ dành cho admin)
    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    // 🔵 Lấy tất cả đơn hàng của user cụ thể
    public List<Order> getOrdersByUserId(Long userId) {
        Customer customer = customerRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return orderRepo.findByCustomer(customer);
    }

    // 🔵 Lấy đơn hàng của user theo trạng thái
    public List<Order> getOrdersByUserIdAndStatus(Long userId, OrderStatus orderStatus) {
        Customer customer = customerRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return orderRepo.findByCustomerAndOrderStatus(customer, orderStatus);
    }

    // 🔵 Lấy đơn hàng theo trạng thái (cho admin)
    public List<Order> getOrdersByStatus(OrderStatus orderStatus) {
        return orderRepo.findByOrderStatus(orderStatus);
    }

    // 🔵 Xem chi tiết đơn hàng
    public Order getOrderById(int id) {
        return orderRepo.findById(id).orElse(null);
    }

    // 🔴 Kiểm tra xem đơn hàng có thể hủy không (trong vòng 30 phút)
    public boolean canCancelOrder(int orderId) {
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        
        // Chỉ có thể hủy đơn hàng có trạng thái WAITING_CONFIRMATION
        if (order.getOrderStatus() != OrderStatus.WAITING_CONFIRMATION) {
            return false;
        }
        
        // Kiểm tra thời gian: chỉ cho phép hủy trong vòng 30 phút
        Date now = new Date();
        Date orderDate = order.getOrderDate();
        long timeDifference = now.getTime() - orderDate.getTime();
        long thirtyMinutesInMillis = 30 * 60 * 1000; // 30 phút tính bằng milliseconds
        
        return timeDifference <= thirtyMinutesInMillis;
    }

    // 🔴 Hủy đơn hàng bởi user (chỉ trong vòng 30 phút)
    public Order cancelOrderByUser(Long userId, int orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));
        
        // Kiểm tra xem đơn hàng có thuộc về user này không
        Customer customer = customerRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (order.getCustomer().getUser().getId() != customer.getUser().getId()) {
            throw new RuntimeException("Unauthorized: Đơn hàng không thuộc về user này");
        }
        
        // Kiểm tra xem có thể hủy đơn hàng không
        if (!canCancelOrder(orderId)) {
            throw new RuntimeException("Không thể hủy đơn hàng. Chỉ có thể hủy trong vòng 30 phút từ lúc đặt hàng và đơn hàng phải ở trạng thái chờ xác nhận.");
        }
        
        // Cập nhật trạng thái đơn hàng thành CANCELLED
        order.setOrderStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepo.save(order);
        
        // 🔔 Send notification
        try {
            String message = String.format("Đơn hàng #%d đã được hủy thành công", orderId);
            notifyService.createNotification(userId, message, "ORDER_CANCELLED", orderId);
            System.out.println("🔔 Notification sent for order #" + orderId + " cancellation by user");
        } catch (Exception e) {
            System.err.println("❌ Failed to send cancellation notification: " + e.getMessage());
        }
        
        return savedOrder;
    }

    // 🔴 Hủy đơn hàng bởi admin (không giới hạn thời gian)
    public Order cancelOrderByAdmin(int orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));
        
        // Admin có thể hủy đơn hàng ở bất kỳ trạng thái nào trừ COMPLETED
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Không thể hủy đơn hàng đã hoàn thành");
        }
        
        order.setOrderStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepo.save(order);
        
        // 🔔 Send notification to customer
        if (order.getCustomer() != null && order.getCustomer().getUser() != null) {
            try {
                Long userId = order.getCustomer().getUser().getId();
                String message = String.format("Đơn hàng #%d đã bị hủy bởi quản trị viên", orderId);
                notifyService.createNotification(userId, message, "ORDER_CANCELLED", orderId);
                System.out.println("🔔 Notification sent for order #" + orderId + " cancellation by admin");
            } catch (Exception e) {
                System.err.println("❌ Failed to send cancellation notification: " + e.getMessage());
            }
        }
        
        return savedOrder;
    }
    public List<Order> searchOrders(String keyword) {
        return orderRepo.searchOrders(keyword.toLowerCase());
    }
    /**
     * 🟢 Đếm số lượng đơn hàng theo trạng thái — chỉ của 1 user
     */
    public Map<OrderStatus, Long> countOrdersByStatusForUser(Long userId) {
        Customer customer = customerRepo.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        List<Order> orders = orderRepo.findByCustomer(customer);
        Map<OrderStatus, Long> stats = new EnumMap<>(OrderStatus.class);

        // Khởi tạo mặc định = 0
        for (OrderStatus status : OrderStatus.values()) {
            stats.put(status, 0L);
        }

        // Đếm thực tế
        for (Order order : orders) {
            stats.put(order.getOrderStatus(), stats.get(order.getOrderStatus()) + 1);
        }

        return stats;
    }

    /**
     * 🟢 Đếm số lượng đơn hàng theo trạng thái — toàn hệ thống (admin)
     */
    public Map<OrderStatus, Long> countAllOrdersByStatus() {
        List<Order> orders = orderRepo.findAll();
        Map<OrderStatus, Long> stats = new EnumMap<>(OrderStatus.class);

        for (OrderStatus status : OrderStatus.values()) {
            stats.put(status, 0L);
        }

        for (Order order : orders) {
            stats.put(order.getOrderStatus(), stats.get(order.getOrderStatus()) + 1);
        }

        return stats;
    }
    public void updateOrderStatus(int orderId, OrderStatus newStatus) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + orderId));
        order.setOrderStatus(newStatus);
        orderRepo.save(order);
    }
}
