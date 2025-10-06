package com.electroshop.controller;

import com.electroshop.entity.*;
import com.electroshop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private CartRepository cartRepository;

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test database connection by counting records
            long accountCount = accountRepository.count();
            long userCount = userRepository.count();
            long customerCount = customerRepository.count();
            long shopCount = shopRepository.count();
            long categoryCount = categoryRepository.count();
            long productCount = productRepository.count();
            long addressCount = addressRepository.count();
            long cartCount = cartRepository.count();
            
            response.put("status", "success");
            response.put("message", "Database connection successful");
            response.put("data", Map.of(
                "accounts", accountCount,
                "users", userCount,
                "customers", customerCount,
                "shops", shopCount,
                "categories", categoryCount,
                "products", productCount,
                "addresses", addressCount,
                "carts", cartCount
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Database connection failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "ElectroShop application is running");
        return ResponseEntity.ok(response);
    }
}


