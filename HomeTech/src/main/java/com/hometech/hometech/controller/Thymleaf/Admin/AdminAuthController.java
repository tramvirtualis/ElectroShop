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
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user-list"; // => src/main/resources/templates/admin/user-list.html
    }

    // 🟡 Cập nhật trạng thái hoạt động của người dùng
    @PostMapping("/dashboard/update-status/{id}")
    public String updateUserStatus(@PathVariable("id") Long id,
                                   @RequestParam("active") boolean active,
                                   Model model) {
        userService.updateUserStatus(id, active);
        model.addAttribute("users", userService.getAllUsers());
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
        model.addAttribute("users", userService.searchUsers(keyword));
        model.addAttribute("keyword", keyword);
        return "admin/user-list";
    }
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        long totalUsers = userService.countAll();
        long activeUsers = userService.countByStatus(true);
        long inactiveUsers = userService.countByStatus(false);

        var products = productService.getAll();
        long activeProductsCount = products.stream().filter(p -> p.isStatus()).count();
        long inactiveProductsCount = products.size() - activeProductsCount;

        model.addAttribute("products", products);
        model.addAttribute("activeProductsCount", activeProductsCount);
        model.addAttribute("inactiveProductsCount", inactiveProductsCount);

        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("users", userService.getAllUsers());

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("inactiveUsers", inactiveUsers);
        model.addAttribute("title", "Bảng điều khiển quản trị");
        return "admin/dashboard"; // ✅ templates/admin/dashboard.html
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
            model.addAttribute("users", userService.getAllUsers());
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
                return "redirect:/admin/dashboard";
            }

            // Đổi trạng thái
            user.setActive(!user.isActive());
            userService.save(user);

            ra.addFlashAttribute("success", "Cập nhật trạng thái người dùng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }


    // 🟣 Đổi vai trò (USER <-> ADMIN)
    @PostMapping("/dashboard/users/change-role/{id}")
    public String changeUserRole(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                ra.addFlashAttribute("error", "Không tìm thấy người dùng!");
                return "redirect:/admin/dashboard";
            }

            if (user.getAccount() == null) {
                ra.addFlashAttribute("error", "Người dùng không có tài khoản hợp lệ!");
                return "redirect:/admin/dashboard";
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

        return "redirect:/admin/dashboard";
    }




}
