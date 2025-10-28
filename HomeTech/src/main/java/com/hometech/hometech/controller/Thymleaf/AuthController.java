package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.model.Account;
import com.hometech.hometech.model.User;
import com.hometech.hometech.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AccountReposirory accountRepository;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, AccountReposirory accountRepository, UserRepository userRepository) {
        this.authService = authService;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    /**
     * 🟢 Trang đăng nhập
     */
    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 🧠 Kiểm tra đúng cách: chỉ redirect nếu thực sự có user đã login
        if (auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() != null
                && !"anonymousUser".equals(auth.getPrincipal().toString())) {
            return "redirect:/";
        }

        HttpSession session = request.getSession(true);
        model.addAttribute("sessionId", session.getId());
        return "auth/login";
    }

    /**
     * 🟢 Trang đăng ký
     */
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    /**
     * 🟢 Xử lý đăng ký
     */
    @PostMapping("/register")
    public String processRegister(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            RedirectAttributes ra) {
        try {
            String msg = authService.register(username, email, password);
            ra.addFlashAttribute("successMessage", msg);
            return "redirect:/auth/login";
        } catch (RuntimeException | MessagingException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    /**
     * 🟢 Đăng nhập bằng Google (OAuth2)
     */
    @GetMapping("/google")
    public String googleLogin() {
        return "redirect:/oauth2/authorization/google";
    }

    /**
     * 🟢 Trang quên mật khẩu
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    /**
     * 🟢 Trang đặt lại mật khẩu
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    /**
     * 🟢 Xác thực email khi người dùng bấm link trong Gmail
     */
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes ra) {
        try {
            String message = authService.verifyEmail(token);
            ra.addFlashAttribute("successMessage", message);
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/login";
        }
    }

    /**
     * 🟢 Xử lý đặt lại mật khẩu
     */
    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword,
            RedirectAttributes ra) {
        try {
            String message = authService.resetPassword(token, newPassword);
            ra.addFlashAttribute("successMessage", message);
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/reset-password?token=" + token;
        }
    }

    /**
     * 🟢 Khi đăng nhập thành công (xử lý session)
     */
    @GetMapping("/login-success")
    public String loginSuccess(HttpServletRequest request, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("sessionId", session.getId());
            session.setAttribute("username", auth.getName());
            session.setAttribute("isAuthenticated", true);
            session.setAttribute("authorities", auth.getAuthorities());

            // Save userId (User.id) in session for downstream controllers
            Account account = accountRepository.findByUsername(auth.getName())
                    .or(() -> accountRepository.findByEmail(auth.getName()))
                    .orElse(null);
            if (account != null) {
                User user = userRepository.findByAccount(account);
                if (user != null) {
                    session.setAttribute("userId", user.getId());
                }
            }

            model.addAttribute("username", auth.getName());
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("sessionId", session.getId());

            return "redirect:/";
        }
        return "redirect:/auth/login";
    }
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes ra) {
        // Hủy session hiện tại
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Xóa context của Spring Security
        SecurityContextHolder.clearContext();

        ra.addFlashAttribute("success", "Đăng xuất thành công!");
        return "redirect:/"; // Quay về trang chủ
    }
}
