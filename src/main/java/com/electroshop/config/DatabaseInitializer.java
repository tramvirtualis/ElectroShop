package com.electroshop.config;

import com.electroshop.entity.*;
import com.electroshop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("!test")
public class DatabaseInitializer implements CommandLineRunner {

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

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Database Initialization Started ===");
        
        // Check if data already exists
        if (accountRepository.count() > 0) {
            System.out.println("Database already initialized. Skipping...");
            return;
        }
        
        try {
            // Create sample accounts
            Account adminAccount = new Account();
            adminAccount.setUsername("admin");
            adminAccount.setPasswordHash("$2a$10$example_hash_here");
            adminAccount.setRole(Account.RoleType.SHOP_OWNER);
            adminAccount = accountRepository.save(adminAccount);
            
            Account customerAccount = new Account();
            customerAccount.setUsername("customer1");
            customerAccount.setPasswordHash("$2a$10$example_hash_here");
            customerAccount.setRole(Account.RoleType.CUSTOMER);
            customerAccount = accountRepository.save(customerAccount);
            
            // Create sample users
            User adminUser = new User();
            adminUser.setAccount(adminAccount);
            adminUser.setFullName("Admin User");
            adminUser.setPhoneNumber("0123456789");
            adminUser.setEmail("admin@electroshop.com");
            adminUser = userRepository.save(adminUser);
            
            User customerUser = new User();
            customerUser.setAccount(customerAccount);
            customerUser.setFullName("John Doe");
            customerUser.setPhoneNumber("0987654321");
            customerUser.setEmail("john.doe@email.com");
            customerUser = userRepository.save(customerUser);
            
            // Create customer
            Customer customer = new Customer();
            customer.setUser(customerUser);
            customer.setBirthdate(LocalDate.of(1990, 1, 15));
            customer = customerRepository.save(customer);
            
            // Create shop
            Shop shop = new Shop();
            shop.setOwnerUser(adminUser);
            shop.setShopName("ElectroShop Main Store");
            shop.setDescription("Main electronics store for ElectroShop platform");
            shop = shopRepository.save(shop);
            
            // Create categories
            Category electronics = new Category();
            electronics.setCategoryName("Electronics");
            electronics.setDescription("Electronic devices and gadgets");
            electronics = categoryRepository.save(electronics);
            
            Category computers = new Category();
            computers.setCategoryName("Computers");
            computers.setDescription("Computer hardware and accessories");
            computers = categoryRepository.save(computers);
            
            // Create products
            Product product1 = new Product();
            product1.setShop(shop);
            product1.setCategory(electronics);
            product1.setProductName("Samsung Galaxy S24");
            product1.setDescription("Latest Samsung smartphone with advanced features");
            product1.setProductPrice(new BigDecimal("999.99"));
            product1.setProductStatus(Product.EnumProduct.SHOW);
            product1.setStockQuantity(50);
            product1 = productRepository.save(product1);
            
            Product product2 = new Product();
            product2.setShop(shop);
            product2.setCategory(computers);
            product2.setProductName("MacBook Pro 14\"");
            product2.setDescription("Apple MacBook Pro with M3 chip");
            product2.setProductPrice(new BigDecimal("1999.99"));
            product2.setProductStatus(Product.EnumProduct.SHOW);
            product2.setStockQuantity(25);
            product2 = productRepository.save(product2);
            
            // Create address
            Address address = new Address();
            address.setCustomer(customer);
            address.setAddressLine("123 Main Street, Apartment 4B");
            address.setCommune("District 1");
            address.setCity("Ho Chi Minh City");
            address.setIsDefault(true);
            address = addressRepository.save(address);
            
            // Create cart
            Cart cart = new Cart();
            cart.setCustomer(customer);
            cart = cartRepository.save(cart);
            
            System.out.println("=== Database Initialization Completed Successfully ===");
            System.out.println("Created:");
            System.out.println("- " + accountRepository.count() + " accounts");
            System.out.println("- " + userRepository.count() + " users");
            System.out.println("- " + customerRepository.count() + " customers");
            System.out.println("- " + shopRepository.count() + " shops");
            System.out.println("- " + categoryRepository.count() + " categories");
            System.out.println("- " + productRepository.count() + " products");
            System.out.println("- " + addressRepository.count() + " addresses");
            System.out.println("- " + cartRepository.count() + " carts");
            
        } catch (Exception e) {
            System.err.println("Error during database initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
