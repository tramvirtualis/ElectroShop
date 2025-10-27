package com.hometech.hometech.controller.Thymleaf.Admin;

import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.dto.UpdateProfileDTO;
import com.hometech.hometech.enums.RoleType;
import com.hometech.hometech.model.Address;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.User;
import com.hometech.hometech.service.AuthService;
import com.hometech.hometech.service.ProfileService;
import com.hometech.hometech.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
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
    public AdminAuthController(AuthService authService, UserService userService,
                               ProfileService profileService) {
        this.authService = authService;
        this.userService = userService;
        this.profileService = profileService;
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
