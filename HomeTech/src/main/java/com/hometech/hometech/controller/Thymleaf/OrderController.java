package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.enums.OrderStatus;
import com.hometech.hometech.model.Order;
import com.hometech.hometech.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {


    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 🧩 Lấy userId từ session (giả định đã lưu khi đăng nhập)
    private Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            return (Long) session.getAttribute("userId");
        }
        throw new RuntimeException("Không tìm thấy userId trong session.");
    }

    // ----------------------------------------------------------
    // 🟢 XEM DANH SÁCH ĐƠN HÀNG
    // ----------------------------------------------------------
    @GetMapping
    public String viewOrders(HttpServletRequest request, Model model) {
        Long userId = getCurrentUserId(request);
        List<Order> orders = orderService.getOrdersByUserId(userId);
        model.addAttribute("orders", orders);
        model.addAttribute("title", "Đơn hàng của tôi");
        model.addAttribute("orderStats", orderService.countOrdersByStatusForUser(userId));

        return "user/orders/index"; // ✅ templates/user/orders/index.html
    }

    // 🟢 XEM CHI TIẾT ĐƠN HÀNG
    @GetMapping("/{orderId}")
    public String viewOrderDetail(@PathVariable int orderId, HttpServletRequest request, Model model) {
        Long userId = getCurrentUserId(request);
        Order order = orderService.getOrderById(orderId);

        // Kiểm tra quyền xem
        if (order == null || order.getCustomer().getUser().getId() != userId) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này.");
        }

        model.addAttribute("order", order);
        model.addAttribute("title", "Chi tiết đơn hàng #" + orderId);
        return "user/orders/detail";
    }

    // ----------------------------------------------------------
    // 🟠 ĐẶT HÀNG TỪ GIỎ HÀNG
    // ----------------------------------------------------------
    @PostMapping("/create")
    public String createOrder(HttpServletRequest request, RedirectAttributes ra) {
        try {
            Long userId = getCurrentUserId(request);
            Order order = orderService.createOrder(userId);
            ra.addFlashAttribute("successMessage", "Đặt hàng thành công! Mã đơn #" + order.getOrderId());
            return "redirect:/user/orders";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    // ----------------------------------------------------------
    // 🔴 HỦY ĐƠN HÀNG (CHỈ TRONG 30 PHÚT)
    // ----------------------------------------------------------
    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable int orderId, HttpServletRequest request, RedirectAttributes ra) {
        try {
            Long userId = getCurrentUserId(request);
            orderService.cancelOrderByUser(userId, orderId);
            ra.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + orderId + " thành công!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/user/orders";
    }

    // ----------------------------------------------------------
    // 🟡 LỌC ĐƠN HÀNG THEO TRẠNG THÁI
    // ----------------------------------------------------------
    @GetMapping("/status/{status}")
    public String viewOrdersByStatus(@PathVariable("status") OrderStatus status,
                                     HttpServletRequest request,
                                     Model model) {
        Long userId = getCurrentUserId(request);
        List<Order> orders = orderService.getOrdersByUserIdAndStatus(userId, status);
        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status);
        model.addAttribute("title", "Đơn hàng trạng thái: " + status);
        model.addAttribute("orderStats", orderService.countOrdersByStatusForUser(userId));

        return "user/orders/index";
    }
    @GetMapping("/search")
    public String searchOrders(@RequestParam("keyword") String keyword,
                               @RequestParam(value = "status", required = false) OrderStatus status,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes ra) {
        try {
            Long userId = getCurrentUserId(request);
            List<Order> results;

            if (status != null) {
                // Nếu đang ở trang lọc trạng thái, chỉ tìm trong các đơn có cùng trạng thái
                results = orderService.getOrdersByUserIdAndStatus(userId, status);
                results.removeIf(order -> !String.valueOf(order.getOrderId()).contains(keyword)
                        && !order.getOrderStatus().name().toLowerCase().contains(keyword.toLowerCase())
                        && !String.valueOf(order.getTotalPrice()).contains(keyword));
                model.addAttribute("currentStatus", status);
                model.addAttribute("title", "Kết quả tìm kiếm trong trạng thái " + status);
            } else {
                // Nếu không có trạng thái cụ thể, tìm toàn bộ đơn của user
                results = orderService.searchOrders(keyword);
                results.removeIf(order -> order.getCustomer().getUser().getId() != userId);
                model.addAttribute("title", "Kết quả tìm kiếm: " + keyword);
            }

            model.addAttribute("orders", results);
            model.addAttribute("keyword", keyword);

            if (results.isEmpty()) {
                model.addAttribute("infoMessage", "Không tìm thấy đơn hàng phù hợp.");
            }

            return "user/orders/index";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi tìm kiếm: " + e.getMessage());
            return "redirect:/orders";
        }
    }
}