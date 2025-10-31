package com.hometech.hometech.controller.Thymleaf.Admin;

import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.controller.Api.AuthRestController;
import com.hometech.hometech.dto.UpdateProfileDTO;
import com.hometech.hometech.enums.RoleType;
import com.hometech.hometech.model.Account;
import com.hometech.hometech.model.Address;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.User;
import com.hometech.hometech.service.AuthService;
import com.hometech.hometech.service.ProfileService;
import com.hometech.hometech.service.ProductService;
import com.hometech.hometech.service.CategoryService;
import com.hometech.hometech.service.OrderService;
import com.hometech.hometech.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    private final AuthService authService;
    private final UserService userService;
    private final ProfileService profileService;
    private final UserDetailsService userDetailsService;
    private final AccountReposirory accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    public AdminAuthController(AuthService authService, UserService userService,
                               ProfileService profileService, UserDetailsService userDetailsService,
                               AccountReposirory accountRepository, PasswordEncoder passwordEncoder,
                               ProductService productService, CategoryService categoryService,
                               OrderService orderService) {
        this.authService = authService;
        this.userService = userService;
        this.profileService = profileService;
        this.userDetailsService = userDetailsService;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
    }

    // 🟢 Hiển thị danh sách người dùng
    @GetMapping("/dashboard/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getUsersWithEmail());
        return "admin/user-list"; // => src/main/resources/templates/admin/user-list.html
    }

    // 🟡 Cập nhật trạng thái hoạt động của người dùng
    @PostMapping("/dashboard/update-status/{id}")
    public String updateUserStatus(@PathVariable("id") Long id,
                                    @RequestParam("active") boolean active,
                                    Model model) {
        userService.updateUserStatus(id, active);
        model.addAttribute("users", userService.getUsersWithEmail());
        model.addAttribute("successMessage", "Cập nhật trạng thái thành công!");
        return "admin/user-list";
    }

//    // 🔵 Đăng ký tài khoản quản trị viên
//    @PostMapping("/register")
//    public String registerAdmin(@RequestParam String username,
//                                @RequestParam String email,
//                                @RequestParam String password,
//                                Model model) {
//        try {
//            authService.registerAdmin(username, email, password);
//            model.addAttribute("successMessage", "Tạo tài khoản quản trị thành công!");
//        } catch (MessagingException e) {
//            model.addAttribute("errorMessage", "Lỗi khi gửi email: " + e.getMessage());
//        } catch (RuntimeException e) {
//            model.addAttribute("errorMessage", e.getMessage());
//        }
//
//        model.addAttribute("users", userService.getAllUsers());
//        return "admin/user-list";
//    }
    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🧠 Kiểm tra đúng cách: chỉ redirect nếu thực sự có user đã login
        if (auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() != null
                && !"anonymousUser".equals(auth.getPrincipal().toString())) {
            return "redirect:/admin/dashboard";
        }

        HttpSession session = request.getSession(true);
        model.addAttribute("sessionId", session.getId());
        return "admin/login";
    }
    @PostMapping("/login")
    public String processAdminLogin(@RequestParam String usernameOrEmail,
                                    @RequestParam String password,
                                    HttpServletRequest request,
                                    RedirectAttributes ra) {
        try {
            // Gọi service đăng nhập admin
            var response = authService.loginAdmin(usernameOrEmail, password);

            // Nếu thành công → tạo session
            HttpSession session = request.getSession(true);
            session.setAttribute("username", response.getUsername());
            session.setAttribute("role", response.getRole());
            session.setAttribute("accessToken", response.getAccessToken());
            session.setAttribute("isAuthenticated", true);

            // Thiết lập Authentication vào SecurityContext để Spring Security nhận diện admin
            UserDetails userDetails = userDetailsService.loadUserByUsername(response.getUsername());
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            // Kiểm tra role (phải là ADMIN)
            if (!"ADMIN".equalsIgnoreCase(response.getRole())) {
                ra.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập vào trang quản trị!");
                return "redirect:/admin/login";
            }

            // ✅ Đăng nhập thành công → chuyển sang trang admin dashboard
            ra.addFlashAttribute("successMessage", "Đăng nhập quản trị viên thành công!");
            return "redirect:/admin/dashboard";

        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/login";
        }
    }


//    Hiển thị tất cả hồ sơ người dùng
//    @GetMapping
//    public String getAllProfiles(Model model) {
//        model.addAttribute("users", userService.getAllUsers());
//        return "admin/profile-list";
//    }
    @PostMapping("/dashboard/update-role/{id}")
    public String updateUserRole(@PathVariable("id") Long id,
                                 @RequestParam("role") RoleType role,
                                 RedirectAttributes ra) {
        try {
            userService.updateUserRole(id, role);
            ra.addFlashAttribute("successMessage", "Cập nhật vai trò thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi cập nhật vai trò: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
    @GetMapping("/dashboard/users/search")
    public String searchUsers(@RequestParam("keyword") String keyword, Model model) {
        // Filter search results to only show users with non-null emails
        List<User> allUsers = userService.searchUsers(keyword);
        List<User> usersWithEmail = allUsers.stream()
                .filter(u -> u.getAccount() != null && u.getAccount().getEmail() != null)
                .toList();
        model.addAttribute("users", usersWithEmail);
        model.addAttribute("keyword", keyword);
        return "admin/user-list";
    }
    @GetMapping("/dashboard")
    public String adminDashboard(@RequestParam(value = "section", required = false) String section,
                                 @RequestParam(value = "search", required = false) String searchKeyword,
                                 @RequestParam(value = "userSearch", required = false) String userSearchKeyword,
                                 @RequestParam(value = "userPage", defaultValue = "0") int userPage,
                                 @RequestParam(value = "userSize", defaultValue = "5") int userSize,
                                 @RequestParam(value = "productPage", defaultValue = "0") int productPage,
                                 @RequestParam(value = "productSize", defaultValue = "10") int productSize,
                                 Model model) {
        long totalUsers = userService.countAll();
        long activeUsers = userService.countByStatus(true);
        long inactiveUsers = userService.countByStatus(false);

        // Get products - filter by search if provided, with pagination
        org.springframework.data.domain.Page<com.hometech.hometech.model.Product> productPageResult;
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            // Search products by name - create manual pagination for search results
            java.util.List<com.hometech.hometech.model.Product> allSearchResults = productService.searchByName(searchKeyword.trim());
            int start = productPage * productSize;
            int end = Math.min(start + productSize, allSearchResults.size());
            java.util.List<com.hometech.hometech.model.Product> paginatedProducts = start < allSearchResults.size() 
                    ? allSearchResults.subList(start, end) 
                    : java.util.Collections.emptyList();
            productPageResult = new org.springframework.data.domain.PageImpl<>(
                    paginatedProducts, 
                    org.springframework.data.domain.PageRequest.of(productPage, productSize), 
                    allSearchResults.size()
            );
            model.addAttribute("searchKeyword", searchKeyword.trim());
        } else {
            productPageResult = productService.getAll(productPage, productSize);
        }
        
        // Count active/inactive from all products (not just current page)
        var allProducts = productService.getAll();
        long activeProductsCount = allProducts.stream().filter(p -> p.isStatus()).count();
        long inactiveProductsCount = allProducts.size() - activeProductsCount;

        model.addAttribute("products", productPageResult.getContent());
        model.addAttribute("productCurrentPage", productPageResult.getNumber());
        model.addAttribute("productTotalPages", productPageResult.getTotalPages());
        model.addAttribute("productTotalElements", productPageResult.getTotalElements());
        model.addAttribute("activeProductsCount", activeProductsCount);
        model.addAttribute("inactiveProductsCount", inactiveProductsCount);

        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("orders", orderService.getAllOrders());
        
        // Get users - filter by search if provided, with pagination
        org.springframework.data.domain.Page<com.hometech.hometech.model.User> userPageResult;
        if (userSearchKeyword != null && !userSearchKeyword.trim().isEmpty()) {
            // Search users by name or email
            java.util.List<com.hometech.hometech.model.User> allUsers = userService.searchUsers(userSearchKeyword.trim());
            // Filter to only show users with non-null emails
            java.util.List<com.hometech.hometech.model.User> filteredUsers = allUsers.stream()
                    .filter(u -> u.getAccount() != null && u.getAccount().getEmail() != null)
                    .toList();
            // For search results, create a paginated list manually
            int start = userPage * userSize;
            int end = Math.min(start + userSize, filteredUsers.size());
            java.util.List<com.hometech.hometech.model.User> paginatedUsers = start < filteredUsers.size() 
                    ? filteredUsers.subList(start, end) 
                    : java.util.Collections.emptyList();
            userPageResult = new org.springframework.data.domain.PageImpl<>(
                    paginatedUsers, 
                    org.springframework.data.domain.PageRequest.of(userPage, userSize), 
                    filteredUsers.size()
            );
            model.addAttribute("userSearchKeyword", userSearchKeyword.trim());
        } else {
            userPageResult = userService.getUsersWithEmail(userPage, userSize);
        }
        model.addAttribute("users", userPageResult.getContent());
        model.addAttribute("userCurrentPage", userPageResult.getNumber());
        model.addAttribute("userTotalPages", userPageResult.getTotalPages());
        model.addAttribute("userTotalElements", userPageResult.getTotalElements());

        // User statistics (chỉ đếm user có email)
        long totalUsersWithEmail = userService.countUsersWithEmail();
        long adminUsers = userService.countUsersWithEmailByRole(RoleType.ADMIN);
        long regularUsers = userService.countUsersWithEmailByRole(RoleType.USER);
        
        // Đếm active/inactive cho user có email
        java.util.List<com.hometech.hometech.model.User> allUsersWithEmail = userService.getUsersWithEmail();
        long activeUsersWithEmail = allUsersWithEmail.stream().filter(u -> u.isActive()).count();
        long inactiveUsersWithEmail = allUsersWithEmail.size() - activeUsersWithEmail;

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("inactiveUsers", inactiveUsers);
        
        // Statistics for users with email
        model.addAttribute("totalUsersWithEmail", totalUsersWithEmail);
        model.addAttribute("activeUsersWithEmail", activeUsersWithEmail);
        model.addAttribute("inactiveUsersWithEmail", inactiveUsersWithEmail);
        model.addAttribute("adminUsers", adminUsers);
        model.addAttribute("regularUsers", regularUsers);
        model.addAttribute("title", "Bảng điều khiển quản trị");
        
        // Set dashboard section if provided
        if (section != null && !section.isEmpty()) {
            model.addAttribute("dashboardSection", section);
        }
        
        return "admin/dashboard";
    }

    // 📊 Trang thống kê tổng hợp
    @GetMapping("/statistics")
    public String statistics(Model model) {
        // Thống kê tổng hợp
        long totalProducts = productService.getAll().size();
        long totalCategories = categoryService.getAll().size();
        long totalOrders = orderService.getAllOrders().size();
        long totalUsersWithEmail = userService.countUsersWithEmail();
        
        // Tính tổng doanh thu từ các đơn hàng đã hoàn thành
        java.util.List<com.hometech.hometech.model.Order> allOrders = orderService.getAllOrders();
        double totalRevenue = allOrders.stream()
                .filter(o -> o.getOrderStatus() == com.hometech.hometech.enums.OrderStatus.COMPLETED)
                .mapToDouble(o -> o.getTotalPrice())
                .sum();
        
        // Thống kê đơn hàng theo trạng thái
        java.util.Map<com.hometech.hometech.enums.OrderStatus, Long> orderStats = orderService.countAllOrdersByStatus();
        
        // Thống kê khách hàng
        long activeUsersWithEmail = userService.getUsersWithEmail().stream()
                .filter(u -> u.isActive())
                .count();
        long inactiveUsersWithEmail = userService.countUsersWithEmail() - activeUsersWithEmail;
        long adminUsers = userService.countUsersWithEmailByRole(RoleType.ADMIN);
        long regularUsers = userService.countUsersWithEmailByRole(RoleType.USER);
        
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalUsersWithEmail", totalUsersWithEmail);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("orderStats", orderStats);
        model.addAttribute("activeUsersWithEmail", activeUsersWithEmail);
        model.addAttribute("inactiveUsersWithEmail", inactiveUsersWithEmail);
        model.addAttribute("adminUsers", adminUsers);
        model.addAttribute("regularUsers", regularUsers);
        
        return "admin/statistics";
    }
    @GetMapping("/register")
    public String registerPage() {
        return "admin/register"; // đường dẫn đến templates/admin/register.html
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam String username,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirect) {
        if (!password.equals(confirmPassword)) {
            redirect.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:/admin/register";
        }

        try {
            // ✅ Gọi service đăng ký admin
            String message = authService.registerAdmin(username, email, password);
            redirect.addFlashAttribute("successMessage", message);
            return "redirect:/admin/login";

        } catch (MessagingException e) {
            redirect.addFlashAttribute("errorMessage", "Không thể gửi email xác thực: " + e.getMessage());
            return "redirect:/admin/register";

        } catch (RuntimeException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/register";
        }
    }
    @GetMapping("/dashboard/users/{id}")
    public String viewUserDetail(@PathVariable("id") Integer id,
                                 Model model,
                                 HttpServletRequest request,
                                 RedirectAttributes ra) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                ra.addFlashAttribute("error", "Không tìm thấy người dùng!");
                return "redirect:/admin/dashboard";
            }

            // 🧩 Thêm dữ liệu cho dashboard
            model.addAttribute("user", user);
            model.addAttribute("users", userService.getUsersWithEmail());
            model.addAttribute("showUserDetail", true);
            model.addAttribute("dashboardSection", "customers");
            model.addAttribute("title", "Chi tiết người dùng");

            // 🟩 Lấy session (nếu cần hiển thị thông tin admin)
            HttpSession session = request.getSession(false);
            if (session != null) {
                model.addAttribute("sessionId", session.getId());
                model.addAttribute("username", session.getAttribute("username"));
            }

            return "admin/dashboard"; // ✅ Render cùng 1 file dashboard.html

        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi tải chi tiết người dùng: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }
    // 🟢 Cập nhật trạng thái người dùng (Active / Inactive)
    @PostMapping("/dashboard/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                ra.addFlashAttribute("error", "Không tìm thấy người dùng!");
                return "redirect:/admin/dashboard?section=customers";
            }

            // Đổi trạng thái
            user.setActive(!user.isActive());
            userService.save(user);

            ra.addFlashAttribute("success", "Cập nhật trạng thái người dùng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?section=customers";
    }


    // 🟣 Đổi vai trò (USER <-> ADMIN)
    @PostMapping("/dashboard/users/change-role/{id}")
    public String changeUserRole(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                ra.addFlashAttribute("error", "Không tìm thấy người dùng!");
                return "redirect:/admin/dashboard?section=customers";
            }

            if (user.getAccount() == null) {
                ra.addFlashAttribute("error", "Người dùng không có tài khoản hợp lệ!");
                return "redirect:/admin/dashboard?section=customers";
            }

            // Đổi vai trò
            if (user.getAccount().getRole() == RoleType.ADMIN) {
                user.getAccount().setRole(RoleType.USER);
            } else {
                user.getAccount().setRole(RoleType.ADMIN);
            }

            userService.save(user);
            ra.addFlashAttribute("success", "Thay đổi vai trò người dùng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi đổi vai trò: " + e.getMessage());
        }

        return "redirect:/admin/dashboard?section=customers";
    }




}
