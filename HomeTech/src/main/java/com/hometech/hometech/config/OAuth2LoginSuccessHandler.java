package com.hometech.hometech.config;

import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.enums.RoleType;
import com.hometech.hometech.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AccountReposirory accountRepository;

    public OAuth2LoginSuccessHandler(AccountReposirory accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Optional<Account> existing = accountRepository.findByEmail(email);
        if (existing.isEmpty()) {
            Account account = new Account();
            account.setEmail(email);
            // Tạo username từ name/email
            String base = (name != null && !name.isBlank())
                    ? name.replaceAll("\\s+", "").toLowerCase()
                    : email.substring(0, email.indexOf('@'));
            account.setUsername(base);
            account.setPassword(""); // không dùng password nội bộ cho oauth
            account.setRole(RoleType.USER);
            account.setEnabled(true);
            account.setEmailVerified(true);
            account.setCreatedAt(LocalDateTime.now());
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
        }

        response.sendRedirect("/");
    }
}
