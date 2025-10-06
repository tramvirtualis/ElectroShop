package com.electroshop.service;

import com.electroshop.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    
    List<Product> getAllProducts();
    
    List<Product> getVisibleProducts();
    
    List<Product> getAvailableProducts();
    
    Optional<Product> getProductById(Long id);
    
    List<Product> getProductsByCategory(Long categoryId);
    
    List<Product> getProductsByShop(Long shopId);
    
    List<Product> searchProductsByName(String name);
    
    Product saveProduct(Product product);
    
    void deleteProduct(Long id);
    
    long getProductCount();
}


