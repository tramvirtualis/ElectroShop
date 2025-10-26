package com.hometech.hometech.controller.Api;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import com.hometech.hometech.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {

    private final CategoryService categoryService;

    public CategoryRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 🟢 Lấy tất cả danh mục
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAll();
        return ResponseEntity.ok(categories);
    }

    // 🟢 Lấy danh mục theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable int id) {
        Category category = categoryService.getById(id);
        if (category != null) {
            return ResponseEntity.ok(category);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 🟢 Lấy danh mục theo tên
    @GetMapping("/name/{categoryName}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String categoryName) {
        Category category = categoryService.getByName(categoryName);
        if (category != null) {
            return ResponseEntity.ok(category);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 🟢 Lấy danh sách sản phẩm trong danh mục
    @GetMapping("/{categoryId}/products")
    public ResponseEntity<List<Product>> getProductsInCategory(@PathVariable int categoryId) {
        List<Product> products = categoryService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    // 🟢 Lấy danh sách sản phẩm đang hoạt động trong danh mục
    @GetMapping("/{categoryId}/products/active")
    public ResponseEntity<List<Product>> getActiveProductsInCategory(@PathVariable int categoryId) {
        List<Product> products = categoryService.getActiveProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    // 🟢 Đếm số lượng sản phẩm trong danh mục
    @GetMapping("/{categoryId}/count")
    public ResponseEntity<Long> countProductsInCategory(@PathVariable int categoryId) {
        long count = categoryService.countProductsInCategory(categoryId);
        return ResponseEntity.ok(count);
    }

    // 🟢 Đếm số lượng sản phẩm đang hoạt động trong danh mục
    @GetMapping("/{categoryId}/count/active")
    public ResponseEntity<Long> countActiveProductsInCategory(@PathVariable int categoryId) {
        long count = categoryService.countActiveProductsInCategory(categoryId);
        return ResponseEntity.ok(count);
    }

    // 🟢 Lấy thông tin chi tiết danh mục (bao gồm số lượng sản phẩm)
    @GetMapping("/{categoryId}/info")
    public ResponseEntity<CategoryInfo> getCategoryInfo(@PathVariable int categoryId) {
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        
        long totalProducts = categoryService.countProductsInCategory(categoryId);
        long activeProducts = categoryService.countActiveProductsInCategory(categoryId);
        
        CategoryInfo info = new CategoryInfo(category, totalProducts, activeProducts);
        return ResponseEntity.ok(info);
    }

    // 🟢 Tạo danh mục mới
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        categoryService.save(category);
        return ResponseEntity.ok(category);
    }

    // 🟢 Cập nhật danh mục
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable int id, @RequestBody Category category) {
        Category existingCategory = categoryService.getById(id);
        if (existingCategory != null) {
            category.setCategoryID(id);
            categoryService.save(category);
            return ResponseEntity.ok(category);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 🟢 Xóa danh mục
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id) {
        Category category = categoryService.getById(id);
        if (category != null) {
            categoryService.delete(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Inner class để trả về thông tin danh mục
    public static class CategoryInfo {
        public final Category category;
        public final long totalProducts;
        public final long activeProducts;

        public CategoryInfo(Category category, long totalProducts, long activeProducts) {
            this.category = category;
            this.totalProducts = totalProducts;
            this.activeProducts = activeProducts;
        }
    }
}