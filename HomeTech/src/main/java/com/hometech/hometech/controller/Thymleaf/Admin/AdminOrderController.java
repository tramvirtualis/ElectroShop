package com.hometech.hometech.controller.Thymleaf.Admin;

import com.hometech.hometech.enums.OrderStatus;
import com.hometech.hometech.model.Order;
import com.hometech.hometech.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ----------------------------------------------------------
    // 🟢 XEM TẤT CẢ ĐƠN HÀNG + THỐNG KÊ
    // ----------------------------------------------------------
    @GetMapping
    public String viewAllOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        Map<OrderStatus, Long> stats = orderService.countAllOrdersByStatus();

        model.addAttribute("orders", orders);
        model.addAttribute("orderStats", stats);
        model.addAttribute("title", "Quản lý đơn hàng");

        return "admin/orders/index"; // ✅ templates/admin/orders/index.html
    }

    // ----------------------------------------------------------
    // 🟢 XEM CHI TIẾT ĐƠN HÀNG
    // ----------------------------------------------------------
    @GetMapping("/{orderId}")
    public String viewOrderDetail(@PathVariable int orderId, Model model, RedirectAttributes ra) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                ra.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng #" + orderId);
                return "redirect:/admin/orders";
            }

            model.addAttribute("order", order);
            model.addAttribute("title", "Chi tiết đơn hàng #" + orderId);
            return "admin/orders/detail";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    // ----------------------------------------------------------
    // 🟡 CẬP NHẬT TRẠNG THÁI
    // ----------------------------------------------------------
    @PostMapping("/update-status")
    public String updateOrderStatus(@RequestParam("orderId") int orderId,
                                    @RequestParam("status") OrderStatus status,
                                    RedirectAttributes ra) {
        try {
            orderService.updateStatus(orderId, status);
            ra.addFlashAttribute("successMessage", "✅ Cập nhật trạng thái đơn hàng #" + orderId + " thành công!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }

    // ----------------------------------------------------------
    // 🔴 HỦY ĐƠN HÀNG (ADMIN)
    // ----------------------------------------------------------
    @PostMapping("/cancel/{orderId}")
    public String cancelOrderByAdmin(@PathVariable int orderId, RedirectAttributes ra) {
        try {
            orderService.cancelOrderByAdmin(orderId);
            ra.addFlashAttribute("successMessage", "🗑 Đã hủy đơn hàng #" + orderId + " thành công!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // ----------------------------------------------------------
    // 🔍 TÌM KIẾM ĐƠN HÀNG
    // ----------------------------------------------------------
    @GetMapping("/search")
    public String searchOrders(@RequestParam("keyword") String keyword, Model model) {
        List<Order> results = orderService.searchOrders(keyword);
        Map<OrderStatus, Long> stats = orderService.countAllOrdersByStatus();

        model.addAttribute("orders", results);
        model.addAttribute("orderStats", stats);
        model.addAttribute("title", "Kết quả tìm kiếm: " + keyword);
        model.addAttribute("keyword", keyword);

        if (results.isEmpty()) {
            model.addAttribute("infoMessage", "Không tìm thấy đơn hàng phù hợp.");
        }

        return "admin/orders/index";
    }

    // ----------------------------------------------------------
    // 🟣 LỌC THEO TRẠNG THÁI
    // ----------------------------------------------------------
    @GetMapping("/status/{status}")
    public String filterByStatus(@PathVariable("status") OrderStatus status, Model model) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        Map<OrderStatus, Long> stats = orderService.countAllOrdersByStatus();

        model.addAttribute("orders", orders);
        model.addAttribute("orderStats", stats);
        model.addAttribute("currentStatus", status);
        model.addAttribute("title", "Đơn hàng trạng thái: " + status);

        return "admin/orders/index";
    }
}
