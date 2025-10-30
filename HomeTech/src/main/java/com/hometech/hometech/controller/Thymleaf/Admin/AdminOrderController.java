package com.hometech.hometech.controller.Thymleaf.Admin;

import com.hometech.hometech.enums.OrderStatus;
import com.hometech.hometech.model.Order;
import com.hometech.hometech.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 🟢 XEM DANH SÁCH / LỌC / XEM CHI TIẾT ĐƠN HÀNG
    @GetMapping("/orders")
    public String viewOrders(@RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "orderId", required = false) int orderId,
                             Model model) {

        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus filter = OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.getOrdersByStatus(filter);
                model.addAttribute("selectedStatus", status.toUpperCase());
            } catch (Exception e) {
                orders = orderService.getAllOrders();
                model.addAttribute("selectedStatus", "ALL");
            }
        } else {
            orders = orderService.getAllOrders();
            model.addAttribute("selectedStatus", "ALL");
        }

        model.addAttribute("orders", orders);
        model.addAttribute("statuses", Arrays.asList(OrderStatus.values()));
        model.addAttribute("dashboardSection", "orders");
        model.addAttribute("title", "Quản lý đơn hàng");

        // Khi click View
        if (orderId >0) {
            Order order = orderService.getOrderById(orderId);
            if (order != null) {
                model.addAttribute("selectedOrder", order);
                model.addAttribute("showOrderDetail", true);
            }
        }

        return "admin/dashboard";
    }

    // 🟢 CẬP NHẬT TRẠNG THÁI
    @PostMapping("/orders/{id}/update-status")
    public String updateOrderStatus(@PathVariable("id") int orderId,
                                    @RequestParam("status") String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            orderService.updateOrderStatus(orderId, newStatus);
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Cập nhật trạng thái đơn hàng #" + orderId + " thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "⚠️ Trạng thái không hợp lệ!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi khi cập nhật đơn hàng!");
        }

        return "redirect:/admin/dashboard/orders";
    }

    // 🔴 HỦY ĐƠN HÀNG
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable("id") int orderId,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("successMessage",
                    "🛑 Đơn hàng #" + orderId + " đã bị hủy!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Lỗi khi hủy đơn hàng #" + orderId);
        }
        return "redirect:/admin/dashboard/orders";
    }
}
