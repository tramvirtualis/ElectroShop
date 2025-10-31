package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.enums.OrderStatus;
import com.hometech.hometech.model.Account;
import com.hometech.hometech.model.Order;
import com.hometech.hometech.model.User;
import com.hometech.hometech.service.OrderService;
import com.hometech.hometech.service.CartService;
import com.hometech.hometech.service.NotifyService;
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
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;
    private final AccountReposirory accountRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotifyService notifyService;

    public OrderController(OrderService service, AccountReposirory accountRepository, UserRepository userRepository, CartService cartService, SimpMessagingTemplate messagingTemplate, NotifyService notifyService) {
        this.service = service;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.messagingTemplate = messagingTemplate;
        this.notifyService = notifyService;
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
    public String showHistory(HttpServletRequest request, Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        
        // If not logged in, show empty list
        if (userId == null) {
            model.addAttribute("orders", java.util.Collections.emptyList());
            model.addAttribute("title", "Lịch sử đơn hàng (khách)");
            model.addAttribute("emptyMessage", "Vui lòng đăng nhập để xem lịch sử đơn hàng.");
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            return "orders/history";
        }
        
        // Get completed and cancelled orders with pagination
        Page<Order> orderPage = service.getCompletedAndCancelledOrdersByUserId(userId, page, size);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", orderPage.getNumber());
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());
        model.addAttribute("title", "Lịch sử đơn hàng của tôi");
        
        // If empty, show message
        if (orderPage.getContent().isEmpty()) {
            model.addAttribute("emptyMessage", "Bạn chưa có đơn hàng đã hoàn thành hoặc đã hủy.");
        }
        
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
    public String listOrders(HttpServletRequest request, Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) {
            // Return empty orders page instead of redirecting
            model.addAttribute("orders", java.util.Collections.emptyList());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            return "orders/index";
        }
        
        // Get active orders with pagination
        Page<Order> orderPage = service.getActiveOrdersByUserId(userId, page, size);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", orderPage.getNumber());
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());
        
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
        System.out.println("🛒 createOrder called");
        Long userId = getCurrentUserId();
        try {
            Order order;
            if (userId != null) {
                System.out.println("👤 Creating order for authenticated user: " + userId);
                // Ensure any session cart items are merged to the user before placing order
                String sessionId = request.getSession(true).getId();
                cartService.mergeSessionCartToUser(sessionId, userId);
                if (shippingAddress != null && !shippingAddress.isBlank()) {
                    order = service.createOrder(userId, shippingAddress);
                } else {
                    order = service.createOrder(userId);
                }
            } else {
                System.out.println("👥 Creating order for guest session");
                String sessionId = request.getSession(true).getId();
                if (shippingAddress != null && !shippingAddress.isBlank()) {
                    order = service.createOrderForSession(sessionId, shippingAddress);
                } else {
                    order = service.createOrderForSession(sessionId);
                }
            }
            
            System.out.println("✅ Order created successfully. Order ID: " + order.getOrderId());
            redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công");
            
            // Send and save notification
            try {
                if (userId != null) {
                    // Save notification to database and send via WebSocket
                    String message = "Đơn hàng #" + order.getOrderId() + " đã được tạo thành công!";
                    notifyService.createNotification(userId, message, "ORDER", order.getOrderId());
                    System.out.println("🔔 Notification saved and sent to user " + userId);
                } else {
                    // Guest user - just send via WebSocket without saving
                    Map<String, String> notification = new HashMap<>();
                    notification.put("message", "Đơn hàng #" + order.getOrderId() + " đã được tạo thành công!");
                    notification.put("timestamp", LocalDateTime.now().toString());
                    messagingTemplate.convertAndSend("/topic/notifications", notification);
                    System.out.println("🔔 Guest notification sent via WebSocket");
                }
                
                // Notify all admins about new order
                String adminMessage = "Đơn hàng mới #" + order.getOrderId() + " đã được tạo!";
                notifyService.notifyAllAdmins(adminMessage, "NEW_ORDER", order.getOrderId());
            } catch (Exception e) {
                System.err.println("❌ Failed to send notification: " + e.getMessage());
                e.printStackTrace();
            }
            
            return "redirect:/orders";
        } catch (RuntimeException e) {
            System.err.println("❌ Error creating order: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
}