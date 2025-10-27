package com.hometech.hometech.service;

import com.hometech.hometech.Repository.CategoryRepository;
import com.hometech.hometech.Repository.ProductRepository;
import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // 🟢 Lấy toàn bộ sản phẩm
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    // 🟢 Lấy sản phẩm theo ID
    public Product getById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    // 🟢 Thêm hoặc cập nhật sản phẩm
    public void save(Product product) {
        productRepository.save(product);
    }

    // 🟢 Xóa sản phẩm
    public void delete(int id) {
        productRepository.deleteById(id);
    }

    // 🟢 Lấy sản phẩm theo danh mục (Category object)
    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category);
    }

    // 🟢 Lấy sản phẩm theo ID danh mục
    public List<Product> getProductsByCategoryId(int categoryId) {
        return productRepository.findByCategory_CategoryID(categoryId);
    }

    // 🟢 Lấy sản phẩm theo tên danh mục
    public List<Product> getProductsByCategoryName(String categoryName) {
        return productRepository.findByCategory_CategoryName(categoryName);
    }

    // 🟢 Lấy sản phẩm đang hoạt động (status = true) theo Category object
    public List<Product> getActiveProductsByCategory(Category category) {
        return productRepository.findByCategoryAndStatus(category, true);
    }

    // 🟢 Lấy sản phẩm đang hoạt động theo ID danh mục
    public List<Product> getActiveProductsByCategoryId(int categoryId) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            return Collections.emptyList(); // Trả về list rỗng thay vì lỗi
        }
        return productRepository.findByCategoryAndStatus(category, true);
    }

    // 🟢 Lấy sản phẩm đang hoạt động theo tên danh mục
    public List<Product> getActiveProductsByCategoryName(String categoryName) {
        Category category = categoryRepository.findAll().stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElse(null);

        if (category == null) {
            return Collections.emptyList();
        }
        return productRepository.findByCategoryAndStatus(category, true);
    }

    // 🟢 Lấy sản phẩm mới thêm trong 7 ngày qua
    public List<Product> getProductsAddedInLast7Days() {
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        return productRepository.findByCreatedAtAfter(lastWeek);
    }

    // 🟢 Lấy top 10 sản phẩm bán chạy nhất
    public List<Product> getTop10BestSellingProducts() {
        return productRepository.findTop10ByOrderBySoldCountDesc();
    }

    // 🟢 Lấy top 10 sản phẩm bán chạy nhất theo tên danh mục
    public List<Product> getTop10BestSellingProductsByCategory(String categoryName) {
        return productRepository.findTop10ByCategory_CategoryNameOrderBySoldCountDesc(categoryName);
    }
}
