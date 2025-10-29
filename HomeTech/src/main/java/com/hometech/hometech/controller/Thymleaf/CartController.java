package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.Repository.CustomerRepository;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.Address;
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
    private final CustomerRepository customerRepository;

    public CartController(CartService service, AccountReposirory accountRepository, UserRepository userRepository, CustomerRepository customerRepository) {
        this.service = service;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
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
    public String viewCart(HttpSession session, HttpServletRequest request, Model model) {
        // ✅ Ưu tiên lấy user từ session (được set khi login)
        User currentUser = (User) session.getAttribute("currentUser");

        // 🟢 Nếu chưa đăng nhập (user == null) → hiển thị giỏ hàng tạm theo session ID
        if (currentUser == null) {
            var items = service.getCartItemsForSession(request.getSession(true).getId());
            model.addAttribute("cartItems", items);

            double totalGuest = items.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            model.addAttribute("totalPrice", totalGuest);
            model.addAttribute("title", "Giỏ hàng tạm");
            return "cart"; // ✅ templates/cart.html hoặc cart/index.html
        }

        // 🟢 Nếu đã đăng nhập → lấy giỏ hàng theo userID
        Long userId = currentUser.getId();
        var userItems = service.getCartItemsByUserId(userId);
        model.addAttribute("cartItems", userItems);

        double total = userItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        model.addAttribute("totalPrice", total);

        // 🏠 Lấy địa chỉ giao hàng của user (nếu có)
        Customer customer = customerRepository.findByUser_Id(userId).orElse(null);
        if (customer != null && customer.getAddress() != null) {
            Address a = customer.getAddress();
            StringBuilder sb = new StringBuilder();

            if (a.getAddressLine() != null && !a.getAddressLine().isBlank()) sb.append(a.getAddressLine());
            if (a.getCommune() != null && !a.getCommune().isBlank()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(a.getCommune());
            }
            if (a.getCity() != null && !a.getCity().isBlank()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(a.getCity());
            }

            model.addAttribute("address", sb.toString());
        }

        model.addAttribute("title", "Giỏ hàng của tôi");
        return "cart"; // ✅ templates/cart.html
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
        if (userId == null) {
            service.increaseQuantityForSession(request.getSession(true).getId(), id);
            return "redirect:/cart";
        }

        service.increaseQuantity(userId, id);
        return "redirect:/cart";
    }

    @GetMapping("/decrease/{id}")
    public String decreaseQuantity(@PathVariable int id,
                                   HttpServletRequest request,
                                   Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) {
            service.decreaseQuantityForSession(request.getSession(true).getId(), id);
            return "redirect:/cart";
        }

        service.decreaseQuantity(userId, id);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{id}")
    public String removeItem(@PathVariable int id,
                             HttpServletRequest request,
                             Model model) {
        addSessionInfo(request, model);
        Long userId = getCurrentUserId();
        if (userId == null) {
            service.removeItemForSession(request.getSession(true).getId(), id);
            return "redirect:/cart";
        }

        service.removeItem(userId, id);
        return "redirect:/cart";
    }
}
