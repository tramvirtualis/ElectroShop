package com.hometech.hometech.controller.Thymleaf.Admin;

import com.hometech.hometech.enums.OrderStatus;
import com.hometech.hometech.model.Order;
import com.hometech.hometech.service.CategoryService;
import com.hometech.hometech.service.OrderService;
import com.hometech.hometech.service.ProductService;
import com.hometech.hometech.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/dashboard/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    public AdminOrderController(OrderService orderService,
                                ProductService productService,
                                CategoryService categoryService,
                                UserService userService) {

        this.orderService = orderService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    // ----------------------------------------------------------
    // 🟢 XEM TẤT CẢ ĐƠN HÀNG + THỐNG KÊ
    // ----------------------------------------------------------
    @GetMapping
    public String viewAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortDir", required = false) String sortDir,
            Model model) {
        Page<Order> orderPage;
        
        if (sortBy != null && !sortBy.trim().isEmpty() && sortDir != null && !sortDir.trim().isEmpty()) {
            orderPage = orderService.getAllOrders(page, size, sortBy.trim(), sortDir.trim());
            model.addAttribute("sortBy", sortBy.trim());
            model.addAttribute("sortDir", sortDir.trim());
        } else {
            orderPage = orderService.getAllOrders(page, size);
        }
        
        Map<OrderStatus, Long> stats = orderService.countAllOrdersByStatus();

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());
        model.addAttribute("orderStats", stats);
        model.addAttribute("title", "Bảng điều khiển quản trị");
        model.addAttribute("dashboardSection", "orders");

        // ✅ Thêm các dữ liệu khác để dashboard không lỗi khi render
        model.addAttribute("products", productService.getAll());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("users", userService.getAllUsers());

        model.addAttribute("totalUsers", userService.countAll());
        model.addAttribute("activeUsers", userService.countByStatus(true));
        model.addAttribute("inactiveUsers", userService.countByStatus(false));
        model.addAttribute("totalOrders", orderPage.getTotalElements());
        model.addAttribute("totalProducts", productService.getAll().size());
        model.addAttribute("totalCategories", categoryService.getAll().size());

        // ✅ Hiển thị tất cả trong 1 trang dashboard
        return "admin/dashboard";
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
                return "redirect:/admin/dashboard/orders";
            }

            model.addAttribute("order", order);
            model.addAttribute("title", "Chi tiết đơn hàng #" + orderId);
            model.addAttribute("orderStatuses", OrderStatus.values());
            return "admin/orders/detail";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/dashboard/orders";
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
        return "redirect:/admin/dashboard/orders/" + orderId;
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
        return "redirect:/admin/dashboard/orders";
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
