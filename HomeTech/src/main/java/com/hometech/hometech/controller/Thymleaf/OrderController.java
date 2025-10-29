package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.enums.OrderStatus;
import com.hometech.hometech.model.Account;
import com.hometech.hometech.model.Order;
import com.hometech.hometech.model.User;
import com.hometech.hometech.service.OrderService;
import com.hometech.hometech.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;
    private final AccountReposirory accountRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final SimpMessagingTemplate messagingTemplate;

    public OrderController(OrderService service, AccountReposirory accountRepository, UserRepository userRepository, CartService cartService, SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.messagingTemplate = messagingTemplate;
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
            String name = authentication.getName();
            // Try by username or email directly
            Account account = accountRepository.findByUsername(name)
                    .or(() -> accountRepository.findByEmail(name))
                    .orElse(null);
            // If OAuth2 and not found, try principal email
            if (account == null && authentication instanceof OAuth2AuthenticationToken oAuth) {
                Object principal = oAuth.getPrincipal();
                if (principal instanceof OAuth2User) {
                    Object emailAttr = ((OAuth2User) principal).getAttributes().get("email");
                    if (emailAttr != null) {
                        account = accountRepository.findByEmail(String.valueOf(emailAttr)).orElse(null);
                    }
                }
            }
            if (account == null) return null;
            User user = userRepository.findByAccount(account);
            return user != null ? user.getId() : null;
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
        // Show only delivered success (COMPLETED) or cancelled
        java.util.List<com.hometech.hometech.model.Order> done = service.getOrdersByUserIdAndStatus(userId, OrderStatus.COMPLETED);
        java.util.List<com.hometech.hometech.model.Order> cancelled = service.getOrdersByUserIdAndStatus(userId, OrderStatus.CANCELLED);
        java.util.List<com.hometech.hometech.model.Order> merged = new java.util.ArrayList<>();
        merged.addAll(done);
        merged.addAll(cancelled);
        // Sort latest first by date
        merged.sort((a,b) -> b.getOrderDate().compareTo(a.getOrderDate()));
        model.addAttribute("orders", merged);
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

    @GetMapping({"", "/", "/index"})
    public String listOrders(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) {
            // Return empty orders page instead of redirecting
            model.addAttribute("orders", java.util.Collections.emptyList());
            return "orders/index";
        }
        // Show only active/in-progress orders (exclude COMPLETED and CANCELLED)
        java.util.List<com.hometech.hometech.model.Order> all = service.getOrdersByUserId(userId);
        java.util.List<com.hometech.hometech.model.Order> active = new java.util.ArrayList<>();
        for (com.hometech.hometech.model.Order o : all) {
            if (o.getOrderStatus() != OrderStatus.COMPLETED && o.getOrderStatus() != OrderStatus.CANCELLED) {
                active.add(o);
            }
        }
        // Sort latest first by date
        active.sort((a,b) -> b.getOrderDate().compareTo(a.getOrderDate()));
        model.addAttribute("orders", active);
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
    public String createOrder(HttpServletRequest request, RedirectAttributes redirectAttributes,
                              @RequestParam(value = "shippingAddress", required = false) String shippingAddress) {
        Long userId = getCurrentUserId();
        try {
            if (userId != null) {
                // Ensure any session cart items are merged to the user before placing order
                String sessionId = request.getSession(true).getId();
                cartService.mergeSessionCartToUser(sessionId, userId);
                Order order;
                if (shippingAddress != null && !shippingAddress.isBlank()) {
                    order = service.createOrder(userId, shippingAddress);
                } else {
                    order = service.createOrder(userId);
                }
                redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công");
                // Notify all clients (Vietnamese message)
                messagingTemplate.convertAndSend("/topic/notifications", new com.hometech.hometech.controller.Api.NotificationController.Notification(
                        "Bạn có đơn hàng mới! Mã đơn #" + order.getOrderId(), java.time.LocalDateTime.now().toString()
                ));
                return "redirect:/orders";
            } else {
                String sessionId = request.getSession(true).getId();
                Order order;
                if (shippingAddress != null && !shippingAddress.isBlank()) {
                    order = service.createOrderForSession(sessionId, shippingAddress);
                } else {
                    order = service.createOrderForSession(sessionId);
                }
                redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công");
                messagingTemplate.convertAndSend("/topic/notifications", new com.hometech.hometech.controller.Api.NotificationController.Notification(
                        "Bạn có đơn hàng mới! Mã đơn #" + order.getOrderId(), java.time.LocalDateTime.now().toString()
                ));
                return "redirect:/orders";
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
}