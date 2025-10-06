package com.electroshop;

import com.electroshop.repository.AccountRepository;
import com.electroshop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ElectroshopApplication.class)
@ActiveProfiles("test")
class ElectroshopApplicationTests {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully
        assertThat(accountRepository).isNotNull();
        assertThat(userRepository).isNotNull();
    }
    
    @Test
    void databaseConnectionTest() {
        // Test database connection by counting records
        long accountCount = accountRepository.count();
        long userCount = userRepository.count();
        
        // These should be non-negative numbers
        assertThat(accountCount).isGreaterThanOrEqualTo(0);
        assertThat(userCount).isGreaterThanOrEqualTo(0);
        
        System.out.println("Database connection test passed!");
        System.out.println("Account count: " + accountCount);
        System.out.println("User count: " + userCount);
    }
}
