package com.hometech.hometech.service;

import com.hometech.hometech.Repository.CategoryRepository;
import com.hometech.hometech.Repository.ProductRepository;
import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repo;
    private final ProductRepository productRepo;

    public CategoryService(CategoryRepository repo, ProductRepository productRepo) {
        this.repo = repo;
        this.productRepo = productRepo;
    }

    public List<Category> getAll() {
        return repo.findAll();
    }

    public Category getById(int id) {
        return repo.findById(id).orElse(null);
    }

    public void save(Category category) {
        repo.save(category);
    }

    public void delete(int id) {
        repo.deleteById(id);
    }

    // 🔹 Lấy danh mục theo tên
    public Category getByName(String categoryName) {
        return repo.findAll().stream()
                .filter(category -> category.getCategoryName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElse(null);
    }

    // 🔹 Lấy danh sách sản phẩm của một danh mục
    public List<Product> getProductsByCategory(int categoryId) {
        Category category = repo.findById(categoryId).orElse(null);
        if (category == null) {
            return List.of();
        }
        return productRepo.findByCategory(category);
    }

    // 🔹 Lấy danh sách sản phẩm đang hoạt động của một danh mục
    public List<Product> getActiveProductsByCategory(int categoryId) {
        Category category = repo.findById(categoryId).orElse(null);
        if (category == null) {
            return List.of();
        }
        return productRepo.findByCategoryAndStatus(category, true);
    }

    // 🔹 Đếm số lượng sản phẩm trong danh mục
    public long countProductsInCategory(int categoryId) {
        Category category = repo.findById(categoryId).orElse(null);
        if (category == null) {
            return 0;
        }
        return productRepo.findByCategory(category).size();
    }

    // 🔹 Đếm số lượng sản phẩm đang hoạt động trong danh mục
    public long countActiveProductsInCategory(int categoryId) {
        Category category = repo.findById(categoryId).orElse(null);
        if (category == null) {
            return 0;
        }
        return productRepo.findByCategoryAndStatus(category, true).size();
    }
}
