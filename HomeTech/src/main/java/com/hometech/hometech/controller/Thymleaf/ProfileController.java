package com.hometech.hometech.controller.Thymleaf;

import com.hometech.hometech.dto.UpdateProfileDTO;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // 🟢 Hiển thị trang profile
    @GetMapping
    public String showProfile(HttpServletRequest request, Model model) {
        addSessionInfo(request, model);
        
        // Get current logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String usernameOrEmailOrId = auth.getName();
            Long userId = profileService.getUserIdByUsername(usernameOrEmailOrId);

            // Fallback for OAuth2: try to resolve by email attribute if lookup by name failed
            String resolvedEmail = null;
            String googleSub = null;
            if (userId == null && auth instanceof OAuth2AuthenticationToken) {
                Object principal = ((OAuth2AuthenticationToken) auth).getPrincipal();
                if (principal instanceof OAuth2User) {
                    OAuth2User oAuth2User = (OAuth2User) principal;
                    Object emailAttr = oAuth2User.getAttributes().get("email");
                    Object subAttr = oAuth2User.getAttributes().get("sub");
                    if (emailAttr != null) {
                        resolvedEmail = String.valueOf(emailAttr);
                        userId = profileService.getUserIdByEmail(resolvedEmail);
                    }
                    if (subAttr != null) {
                        googleSub = String.valueOf(subAttr);
                    }
                }
            }

            if (userId != null) {
                // Link Google ID if missing and we have it
                if (googleSub == null && auth instanceof OAuth2AuthenticationToken) {
                    // Use auth name as a last resort (often Google ID)
                    googleSub = usernameOrEmailOrId;
                }
                if (googleSub != null) {
                    profileService.linkGoogleIdIfMissing(userId, googleSub);
                }

                UpdateProfileDTO profile = profileService.getProfile(userId);
                model.addAttribute("userId", userId);
                model.addAttribute("profile", profile);

                // Debug info
                String dbg = "User ID: " + userId + ", Auth Name: " + usernameOrEmailOrId;
                if (resolvedEmail != null) {
                    dbg += ", OAuth2 Email: " + resolvedEmail;
                }
                if (googleSub != null) {
                    dbg += ", Google Sub: " + googleSub;
                }
                model.addAttribute("debug", dbg);
            } else {
                model.addAttribute("error", "Không tìm thấy thông tin người dùng. Auth name: " + usernameOrEmailOrId);
            }
        }
        
        return "profile";
    }
    
    // 🟡 Update profile endpoint
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute UpdateProfileDTO profileDTO,
                               @RequestParam(value = "picture", required = false) MultipartFile picture,
                               HttpServletRequest request,
                               Model model) {
        addSessionInfo(request, model);
        
        // Get current logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String usernameOrEmail = auth.getName();
            Long userId = profileService.getUserIdByUsername(usernameOrEmail);
            
            if (userId != null) {
                try {
                    if (picture != null && !picture.isEmpty()) {
                        String newUrl = profileService.storeProfileImage(userId, picture);
                        profileDTO.setPictureUrl(newUrl);
                    }
                    profileService.updateOrCreateProfile(userId, profileDTO);
                    model.addAttribute("message", "Cập nhật thông tin thành công!");
                    model.addAttribute("userId", userId);
                    model.addAttribute("profile", profileService.getProfile(userId));
                } catch (Exception e) {
                    e.printStackTrace(); // Log the full error
                    model.addAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
                    model.addAttribute("profile", profileDTO);
                }
            } else {
                model.addAttribute("error", "Không tìm thấy người dùng để cập nhật");
                model.addAttribute("profile", profileDTO);
            }
        }
        
        return "profile";
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
         profileService.updateOrCreateProfile(userId, dto);
        model.addAttribute("message", "Cập nhật thông tin cá nhân thành công!");
        model.addAttribute("profile", profileService.getProfile(userId));
        model.addAttribute("userId", userId);
        return "profile_form";
    }

}
