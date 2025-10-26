package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.service.AuthService;
import com.hometech.hometech.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    public UserController(UserService userService,
                          AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    // 🟢 Hiển thị danh sách người dùng
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user-list"; // => src/main/resources/templates/admin/user-list.html
    }

    // 🟡 Cập nhật trạng thái hoạt động
    @PostMapping("/update-status/{id}")
    public String updateUserStatus(@PathVariable("id") Long id,
                                   @RequestParam("active") boolean active,
                                   Model model) {
        userService.updateUserStatus(id, active);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("successMessage", "Cập nhật trạng thái thành công!");
        return "admin/user-list";
    }
    // 🟡 Xử lý form đăng ký admin
    @PostMapping("/register")
    public String registerAdmin(@RequestParam("username") String username,
                                @RequestParam("email") String email,
                                @RequestParam("password") String password,
                                RedirectAttributes redirectAttributes) {
        try {
            String message = authService.registerAdmin(username, email, password);
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/admin/auth/register?success";
        } catch (MessagingException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi gửi email: " + e.getMessage());
            return "redirect:/admin/auth/register?error";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/auth/register?error";
        }
    }
}
