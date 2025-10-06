package com.electroshop.service.impl;

import com.electroshop.entity.Product;
import com.electroshop.repository.ProductRepository;
import com.electroshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getVisibleProducts() {
        return productRepository.findVisibleProductsOrderByCreatedAt();
    }

    @Override
    public List<Product> getAvailableProducts() {
        return productRepository.findAvailableProducts();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategory_Id(categoryId);
    }

    @Override
    public List<Product> getProductsByShop(Long shopId) {
        return productRepository.findByShop_Id(shopId);
    }

    @Override
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByProductNameContaining(name);
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public long getProductCount() {
        return productRepository.count();
    }
}


