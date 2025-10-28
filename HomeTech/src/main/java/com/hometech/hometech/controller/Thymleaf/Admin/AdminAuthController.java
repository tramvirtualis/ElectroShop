package com.hometech.hometech.controller.Thymleaf.Admin;

import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.controller.Api.AuthRestController;
import com.hometech.hometech.dto.UpdateProfileDTO;
import com.hometech.hometech.enums.RoleType;
import com.hometech.hometech.model.Address;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.User;
import com.hometech.hometech.service.AuthService;
import com.hometech.hometech.service.ProfileService;
import com.hometech.hometech.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    public AdminAuthController(AuthService authService, UserService userService,
                               ProfileService profileService, UserDetailsService userDetailsService) {
        this.authService = authService;
        this.userService = userService;
        this.profileService = profileService;
        this.userDetailsService = userDetailsService;
    }

    // 🟢 Hiển thị danh sách người dùng
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user-list"; // => src/main/resources/templates/admin/user-list.html
    }

    // 🟡 Cập nhật trạng thái hoạt động của người dùng
    @PostMapping("/update-status/{id}")
    public String updateUserStatus(@PathVariable("id") Long id,
                                   @RequestParam("active") boolean active,
                                   Model model) {
        userService.updateUserStatus(id, active);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("successMessage", "Cập nhật trạng thái thành công!");
        return "admin/user-list";
    }

    // 🔵 Đăng ký tài khoản quản trị viên
    @PostMapping("/register")
    public String registerAdmin(@RequestParam String username,
                                @RequestParam String email,
                                @RequestParam String password,
                                Model model) {
        try {
            authService.registerAdmin(username, email, password);
            model.addAttribute("successMessage", "Tạo tài khoản quản trị thành công!");
        } catch (MessagingException e) {
            model.addAttribute("errorMessage", "Lỗi khi gửi email: " + e.getMessage());
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        model.addAttribute("users", userService.getAllUsers());
        return "admin/user-list";
    }
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

    @GetMapping("/{userId}")
    public String getUserProfile(@PathVariable Long userId, Model model) {
        try {
            UpdateProfileDTO profile = profileService.getProfile(userId);
            model.addAttribute("profile", profile);
            model.addAttribute("userId", userId);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "admin/profile-detail"; // 🔹 file Thymeleaf: templates/admin/profile-detail.html
    }
//    Hiển thị tất cả hồ sơ người dùng
    @GetMapping
    public String getAllProfiles(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/profile-list"; // 🔹 file Thymeleaf: templates/admin/profile-list.html
    }
    @PostMapping("/update-role/{id}")
    public String updateUserRole(@PathVariable("id") Long id,
                                 @RequestParam("role") RoleType role,
                                 RedirectAttributes ra) {
        try {
            userService.updateUserRole(id, role);
            ra.addFlashAttribute("successMessage", "Cập nhật vai trò thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi cập nhật vai trò: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    @GetMapping("/users/search")
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

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("inactiveUsers", inactiveUsers);
        model.addAttribute("title", "Bảng điều khiển quản trị");
        return "admin/dashboard"; // ✅ templates/admin/dashboard.html
    }


}
