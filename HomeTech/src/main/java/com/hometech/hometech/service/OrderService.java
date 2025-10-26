package com.hometech.hometech.service;

import com.hometech.hometech.Repository.CartItemRepository;
import com.hometech.hometech.Repository.CustomerRepository;
import com.hometech.hometech.Repository.OrderItemRepository;
import com.hometech.hometech.Repository.OrderRepository;
import com.hometech.hometech.model.CartItem;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.Order;
import com.hometech.hometech.enums.OrderStatus;

import com.hometech.hometech.model.OrderItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final CartItemRepository cartRepo;
    private final CustomerRepository customerRepo;

    public OrderService(OrderRepository orderRepo, OrderItemRepository orderItemRepo,
                        CartItemRepository cartRepo, CustomerRepository customerRepo) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.cartRepo = cartRepo;
        this.customerRepo = customerRepo;
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

    // 🟡 Cập nhật trạng thái đơn hàng
    public Order updateStatus(int orderId, OrderStatus newStatus) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));
        order.setOrderStatus(newStatus);
        return orderRepo.save(order);
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
        return orderRepo.save(order);
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
        return orderRepo.save(order);
    }
}
