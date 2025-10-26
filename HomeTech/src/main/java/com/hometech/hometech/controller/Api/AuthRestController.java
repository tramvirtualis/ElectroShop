package com.hometech.hometech.controller.Api;

import com.hometech.hometech.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthRestController {

    private final AuthService authService;

    public AuthRestController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String message = authService.register(request.getUsername(), request.getEmail(), request.getPassword());
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            response.put("success", false);
            response.put("message", "Lỗi gửi email xác thực. Vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam("token") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String message = authService.verifyEmail(token);
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            AuthService.AuthResponse authResponse = authService.login(request.getUsernameOrEmail(), request.getPassword());
            
            response.put("success", true);
            response.put("message", authResponse.getMessage());
            response.put("data", Map.of(
                "accessToken", authResponse.getAccessToken(),
                "refreshToken", authResponse.getRefreshToken(),
                "username", authResponse.getUsername(),
                "email", authResponse.getEmail(),
                "role", authResponse.getRole()
            ));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String message = authService.forgotPassword(request.getEmail());
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            response.put("success", false);
            response.put("message", "Lỗi gửi email. Vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String message = authService.resetPassword(request.getToken(), request.getNewPassword());
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String newAccessToken = authService.refreshToken(request.getRefreshToken());
            response.put("success", true);
            response.put("message", "Làm mới token thành công");
            response.put("data", Map.of("accessToken", newAccessToken));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Xóa authentication context
            SecurityContextHolder.clearContext();
            
            // Invalidate session nếu có
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            response.put("success", true);
            response.put("message", "Đăng xuất thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi đăng xuất");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Request DTOs
    public static class RegisterRequest {
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3-50 ký tự")
        private String username;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        private String email;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
        private String password;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
        private String usernameOrEmail;

        @NotBlank(message = "Mật khẩu không được để trống")
        private String password;

        // Getters and Setters
        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        private String email;

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ResetPasswordRequest {
        @NotBlank(message = "Token không được để trống")
        private String token;

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
        private String newPassword;

        // Getters and Setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token không được để trống")
        private String refreshToken;

        // Getters and Setters
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    // ===============================
    // OAUTH2 ENDPOINTS
    // ===============================

    @GetMapping("/oauth2/user")
    public ResponseEntity<Map<String, Object>> getOAuth2User(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("success", false);
            response.put("message", "Người dùng chưa đăng nhập bằng OAuth2");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String email = principal.getAttribute("email");
            String name = principal.getAttribute("name");
            String picture = principal.getAttribute("picture");
            String sub = principal.getAttribute("sub");

            response.put("success", true);
            response.put("message", "Lấy thông tin OAuth2 user thành công");
            response.put("data", Map.of(
                "email", email != null ? email : "",
                "name", name != null ? name : "",
                "picture", picture != null ? picture : "",
                "googleId", sub != null ? sub : "",
                "provider", "google"
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thông tin OAuth2 user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "Người dùng chưa đăng nhập");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        response.put("success", true);
        response.put("message", "Lấy thông tin user thành công");
        response.put("data", Map.of(
            "username", userDetails.getUsername(),
            "authorities", userDetails.getAuthorities(),
            "enabled", userDetails.isEnabled(),
            "accountNonExpired", userDetails.isAccountNonExpired(),
            "accountNonLocked", userDetails.isAccountNonLocked(),
            "credentialsNonExpired", userDetails.isCredentialsNonExpired()
        ));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth2/login-info")
    public ResponseEntity<Map<String, Object>> getOAuth2LoginInfo() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("success", true);
        response.put("message", "Thông tin đăng nhập OAuth2");
        response.put("data", Map.of(
            "googleLoginUrl", "/oauth2/authorization/google",
            "redirectUri", "http://localhost:8080/login/oauth2/code/google",
            "scopes", new String[]{"profile", "email"},
            "instructions", Map.of(
                "step1", "Truy cập /oauth2/authorization/google để bắt đầu OAuth2 flow",
                "step2", "Đăng nhập với tài khoản Google",
                "step3", "Hệ thống sẽ tự động redirect và tạo JWT tokens",
                "step4", "Sử dụng JWT tokens để gọi các API khác"
            )
        ));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/oauth2/test-login")
    public ResponseEntity<Map<String, Object>> testOAuth2Login() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("success", true);
        response.put("message", "Hướng dẫn test OAuth2 Login");
        response.put("instructions", Map.of(
            "method", "Không thể test OAuth2 trực tiếp bằng Postman",
            "reason", "OAuth2 flow yêu cầu browser để redirect",
            "solution", "Sử dụng browser để truy cập /oauth2/authorization/google",
            "alternative", "Sử dụng Postman OAuth2 Authorization Code flow",
            "steps", new String[]{
                "1. Mở browser và truy cập: http://localhost:8080/oauth2/authorization/google",
                "2. Đăng nhập với tài khoản Google",
                "3. Copy JWT token từ response",
                "4. Sử dụng JWT token trong Postman với header: Authorization: Bearer {token}"
            }
        ));
        
        return ResponseEntity.ok(response);
    }
}
