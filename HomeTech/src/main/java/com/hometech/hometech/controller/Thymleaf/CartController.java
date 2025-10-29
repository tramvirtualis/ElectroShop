package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.model.Account;
import com.hometech.hometech.model.User;
import com.hometech.hometech.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService service;
    private final AccountReposirory accountRepository;
    private final UserRepository userRepository;

    public CartController(CartService service, AccountReposirory accountRepository, UserRepository userRepository) {
        this.service = service;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
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
                    .map((Account account) -> {
                        User u = userRepository.findByAccount(account);
                        return u != null ? u.getId() : null;
                    })
                    .orElse(null);
        }
        return null;
    }

    @GetMapping
    public String viewCart(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) {
            // Return empty cart page instead of redirecting
            model.addAttribute("cartItems", java.util.Collections.emptyList());
            model.addAttribute("totalPrice", 0.0);
            return "cart";
        }

        model.addAttribute("cartItems", service.getCartItemsByUserId(userId));
        double total = service.getCartItemsByUserId(userId)
                .stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        model.addAttribute("totalPrice", total);
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam int productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpServletRequest request,
                            Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) {
            service.addProductForSession(request.getSession(true).getId(), productId, quantity);
            return "redirect:/cart";
        }

        service.addProduct(userId, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/add-ajax")
    @ResponseBody
    public String addToCartAjax(@RequestParam int productId,
                                @RequestParam(defaultValue = "1") int quantity,
                                HttpServletRequest request,
                                Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) {
            service.addProductForSession(request.getSession(true).getId(), productId, quantity);
            return "OK";
        }
        service.addProduct(userId, productId, quantity);
        return "OK";
    }

    @GetMapping("/increase/{id}")
    public String increaseQuantity(@PathVariable int id,
                                   HttpServletRequest request,
                                   Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) return "redirect:/auth/login";

        service.increaseQuantity(userId, id);
        return "redirect:/cart";
    }

    @GetMapping("/decrease/{id}")
    public String decreaseQuantity(@PathVariable int id,
                                   HttpServletRequest request,
                                   Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) return "redirect:/auth/login";

        service.decreaseQuantity(userId, id);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{id}")
    public String removeItem(@PathVariable int id,
                             HttpServletRequest request,
                             Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) return "redirect:/auth/login";

        service.removeItem(userId, id);
        return "redirect:/cart";
    }
}
