package com.hometech.hometech.controller.Api;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import com.hometech.hometech.service.CategoryService;
import com.hometech.hometech.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductRestController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // 🟢 Lấy tất cả sản phẩm
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAll();
        return ResponseEntity.ok(products);
    }

    // 🟢 Lấy sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        Product product = productService.getById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 🟢 Lấy sản phẩm theo ID danh mục
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategoryId(@PathVariable int categoryId) {
        List<Product> products = productService.getProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    // 🟢 Lấy sản phẩm đang hoạt động theo ID danh mục
    @GetMapping("/category/{categoryId}/active")
    public ResponseEntity<List<Product>> getActiveProductsByCategoryId(@PathVariable int categoryId) {
        List<Product> products = productService.getActiveProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    // 🟢 Lấy sản phẩm theo tên danh mục
    @GetMapping("/category/name/{categoryName}")
    public ResponseEntity<List<Product>> getProductsByCategoryName(@PathVariable String categoryName) {
        List<Product> products = productService.getProductsByCategoryName(categoryName);
        return ResponseEntity.ok(products);
    }

    // 🟢 Lấy sản phẩm đang hoạt động theo tên danh mục
    @GetMapping("/category/name/{categoryName}/active")
    public ResponseEntity<List<Product>> getActiveProductsByCategoryName(@PathVariable String categoryName) {
        List<Product> products = productService.getActiveProductsByCategoryName(categoryName);
        return ResponseEntity.ok(products);
    }

    // 🟢 Tạo sản phẩm mới
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        productService.save(product);
        return ResponseEntity.ok(product);
    }

    // 🟢 Cập nhật sản phẩm
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable int id, @RequestBody Product product) {
        Product existingProduct = productService.getById(id);
        if (existingProduct != null) {
            product.setProductID(id);
            productService.save(product);
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 🟢 Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        Product product = productService.getById(id);
        if (product != null) {
            productService.delete(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 🟢 Lấy thông tin danh mục và số lượng sản phẩm
    @GetMapping("/category/{categoryId}/info")
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