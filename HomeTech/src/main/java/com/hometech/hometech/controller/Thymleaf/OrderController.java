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

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;
    private final AccountReposirory accountRepository;

    public OrderController(OrderService service, AccountReposirory accountRepository) {
        this.service = service;
        this.accountRepository = accountRepository;
    }

    private void addSessionInfo(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            model.addAttribute("sessionId", session.getId());
            model.addAttribute("username", session.getAttribute("username"));
            model.addAttribute("isAuthenticated", session.getAttribute("isAuthenticated"));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            model.addAttribute("currentUser", auth.getName());
            model.addAttribute("userAuthorities", auth.getAuthorities());
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return accountRepository.findByUsername(username)
                    .map(account -> account.getAccountId())
                    .orElse(null);
        }
        return null;
    }

    @GetMapping("/history")
    public String showHistory(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) {
            // Return empty history page instead of redirecting
            model.addAttribute("orders", java.util.Collections.emptyList());
            return "orders/history";
        }
        model.addAttribute("orders", service.getOrdersByUserId(userId));
        return "orders/history";
    }

    @GetMapping("/status/{status}")
    public String listOrdersByStatus(@PathVariable OrderStatus status,
                                     HttpServletRequest request,
                                     Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) return "redirect:/auth/login";

        model.addAttribute("orders", service.getOrdersByUserIdAndStatus(userId, status));
        model.addAttribute("currentStatus", status);
        model.addAttribute("statuses", OrderStatus.values());
        return "orders/status";
    }

    @GetMapping({"/", "/index"})
    public String listOrders(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) {
            // Return empty orders page instead of redirecting
            model.addAttribute("orders", java.util.Collections.emptyList());
            return "orders/index";
        }
        model.addAttribute("orders", service.getOrdersByUserId(userId));
        return "orders/index";
    }

    @GetMapping("/{id}")
    public String viewOrderDetail(@PathVariable int id,
                                  HttpServletRequest request,
                                  Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) return "redirect:/auth/login";

        Order order = service.getOrderById(id);
        if (order == null) return "redirect:/orders";

        if (order.getCustomer().getUser().getId() != userId) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("canCancel", service.canCancelOrder(id));
        return "orders/detail";
    }

    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable int id,
                                    @RequestParam("status") OrderStatus status) {
        service.updateStatus(id, status);
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable int id) {
        Long userId = getCurrentUserId();
        if (userId == null) return "redirect:/auth/login";
        try {
            service.cancelOrderByUser(userId, id);
            return "redirect:/orders?success=Canceled successfully";
        } catch (RuntimeException e) {
            return "redirect:/orders/" + id + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/create")
    public String createOrder() {
        Long userId = getCurrentUserId();
        if (userId == null) return "redirect:/auth/login";
        try {
            service.createOrder(userId);
            return "redirect:/orders";
        } catch (RuntimeException e) {
            return "redirect:/cart?error=" + e.getMessage();
        }
    }
}
