package com.hometech.hometech.config;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import com.hometech.hometech.service.CategoryService;
import com.hometech.hometech.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DataInitializer.class);
    
    private final CategoryService categoryService;
    private final ProductService productService;

    public DataInitializer(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Starting data initialization...");
        
        // Initialize categories
        initializeCategories();
        
        // Initialize products
        initializeProducts();
        
        LOG.info("Data initialization completed!");
    }

    private void initializeCategories() {
        LOG.info("Initializing categories...");
        
        List<String> categoryNames = Arrays.asList("Smartphone", "Laptop", "Tablet", "Smart Watch");
        
        for (String categoryName : categoryNames) {
            // Check if category already exists
            List<Category> existingCategories = categoryService.getAll();
            boolean categoryExists = existingCategories.stream()
                    .anyMatch(cat -> cat.getCategoryName().equals(categoryName));
            
            if (!categoryExists) {
                Category category = new Category();
                category.setCategoryName(categoryName);
                categoryService.save(category);
                LOG.info("Created category: {}", categoryName);
            } else {
                LOG.info("Category already exists: {}", categoryName);
            }
        }
    }

    private void initializeProducts() {
        LOG.info("Initializing products...");
        
        // Check if products already exist
        List<Product> existingProducts = productService.getAll();
        if (!existingProducts.isEmpty()) {
            LOG.info("Products already exist ({} products), skipping initialization", existingProducts.size());
            return;
        }
        
        // Get categories
        List<Category> categories = categoryService.getAll();
        if (categories.isEmpty()) {
            LOG.warn("No categories found, cannot initialize products");
            return;
        }
        
        Category smartphoneCategory = categories.stream()
                .filter(cat -> cat.getCategoryName().equals("Smartphone"))
                .findFirst().orElse(categories.get(0));
        
        Category laptopCategory = categories.stream()
                .filter(cat -> cat.getCategoryName().equals("Laptop"))
                .findFirst().orElse(categories.get(0));
        
        Category tabletCategory = categories.stream()
                .filter(cat -> cat.getCategoryName().equals("Tablet"))
                .findFirst().orElse(categories.get(0));
        
        Category smartWatchCategory = categories.stream()
                .filter(cat -> cat.getCategoryName().equals("Smart Watch"))
                .findFirst().orElse(categories.get(0));

        // Create sample products
        List<Product> sampleProducts = Arrays.asList(
            // Smartphones
            createProduct("iPhone 15 Pro", 25990000, "iPhone 15 Pro với chip A17 Pro mạnh mẽ", smartphoneCategory, 150),
            createProduct("Samsung Galaxy S24 Ultra", 28990000, "Galaxy S24 Ultra với camera 200MP", smartphoneCategory, 120),
            createProduct("Google Pixel 9 Pro", 22990000, "Pixel 9 Pro với AI tích hợp", smartphoneCategory, 80),
            createProduct("OnePlus 12", 19990000, "OnePlus 12 với Snapdragon 8 Gen 3", smartphoneCategory, 60),
            
            // Laptops
            createProduct("MacBook Pro M3", 45990000, "MacBook Pro với chip M3 Pro", laptopCategory, 90),
            createProduct("Dell XPS 15", 35990000, "Dell XPS 15 với Intel Core i7", laptopCategory, 70),
            createProduct("ASUS ROG Strix", 32990000, "ASUS ROG Strix gaming laptop", laptopCategory, 50),
            
            // Tablets
            createProduct("iPad Pro 12.9\"", 25990000, "iPad Pro với chip M2", tabletCategory, 100),
            createProduct("Samsung Galaxy Tab S9", 18990000, "Galaxy Tab S9 với S Pen", tabletCategory, 40),
            
            // Smart Watches
            createProduct("Apple Watch Series 9", 8990000, "Apple Watch Series 9 với chip S9", smartWatchCategory, 200),
            createProduct("Samsung Galaxy Watch 6", 6990000, "Galaxy Watch 6 với Wear OS", smartWatchCategory, 80),
            createProduct("Google Pixel Watch 2", 7990000, "Pixel Watch 2 với Fitbit", smartWatchCategory, 60)
        );

        // Save products
        for (Product product : sampleProducts) {
            productService.save(product);
            LOG.info("Created product: {} - {} VNĐ", product.getProductName(), product.getPrice());
        }
        
        LOG.info("Created {} sample products", sampleProducts.size());
    }

    private Product createProduct(String name, double price, String description, Category category, int soldCount) {
        Product product = new Product();
        product.setProductName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setCategory(category);
        product.setSoldCount(soldCount);
        product.setStatus(true);
        product.setColor("Black");
        product.setSize(0);
        return product;
    }
}




