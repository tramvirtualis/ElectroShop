package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.dto.UpdateProfileDTO;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
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

    // 🟢 Hiển thị form cập nhật thông tin cá nhân
    @GetMapping("/{userId}")
    public String showProfileForm(@PathVariable Long userId,
                                  HttpServletRequest request,
                                  Model model) {
        addSessionInfo(request, model);
        UpdateProfileDTO dto = profileService.getProfile(userId);
        model.addAttribute("userId", userId);
        model.addAttribute("profile", dto);
        return "profile_form";
    }

    // 🟡 Xử lý submit form (thêm mới hoặc cập nhật)
    @PostMapping("/{userId}")
    public String updateProfile(@PathVariable Long userId,
                                @ModelAttribute("profile") UpdateProfileDTO dto,
                                HttpServletRequest request,
                                Model model) {
        addSessionInfo(request, model);
        Customer saved = profileService.updateOrCreateProfile(userId, dto);
        model.addAttribute("message", "Cập nhật thông tin cá nhân thành công!");
        model.addAttribute("profile", profileService.getProfile(userId));
        model.addAttribute("userId", userId);
        return "profile_form";
    }

}
