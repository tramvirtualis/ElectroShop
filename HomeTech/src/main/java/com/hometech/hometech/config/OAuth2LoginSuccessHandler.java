package com.hometech.hometech.config;

import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.enums.RoleType;
import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.Repository.CustomerRepository;
import com.hometech.hometech.model.User;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.Cart;
import com.hometech.hometech.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import com.hometech.hometech.service.CartService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AccountReposirory accountRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CartService cartService;

    public OAuth2LoginSuccessHandler(AccountReposirory accountRepository,
                                     UserRepository userRepository,
                                     CustomerRepository customerRepository,
                                     CartService cartService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.cartService = cartService;
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
        Account account = existing.orElseGet(() -> {
            Account acc = new Account();
            acc.setEmail(email);
            String base = (name != null && !name.isBlank())
                    ? name.replaceAll("\\s+", "").toLowerCase()
                    : email.substring(0, email.indexOf('@'));
            acc.setUsername(base);
            acc.setPassword("");
            acc.setRole(RoleType.USER);
            acc.setEnabled(true);
            acc.setEmailVerified(true);
            acc.setCreatedAt(LocalDateTime.now());
            acc.setUpdatedAt(LocalDateTime.now());
            return accountRepository.save(acc);
        });

        // Ensure User exists and is linked to Account
        User user = userRepository.findByAccount(account);
        if (user == null) {
            user = new User();
            user.setAccount(account);
        }
        user.setEmail(email);
        user.setName(name);
        user.setActive(true);
        userRepository.save(user);

        // Ensure Customer profile and Cart exist
        Customer customer = customerRepository.findByUser_Id(user.getId()).orElse(null);
        if (customer == null) {
            customer = new Customer();
            customer.setUser(user);
            Cart cart = new Cart();
            cart.setCustomer(customer);
            customer.setCart(cart);
            customerRepository.save(customer);
        } else if (customer.getCart() == null) {
            Cart cart = new Cart();
            cart.setCustomer(customer);
            customer.setCart(cart);
            customerRepository.save(customer);
        }

        // Merge any session cart accumulated before login
        String sessionId = request.getSession(true).getId();
        cartService.mergeSessionCartToUser(sessionId, user.getId());

        response.sendRedirect("/");
    }
}
