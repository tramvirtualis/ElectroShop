package com.hometech.hometech.Repository;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    // Basic category queries
    List<Product> findByCategory(Category category);
    List<Product> findByCategory_CategoryID(int categoryId);
    List<Product> findByCategory_CategoryName(String categoryName);
    List<Product> findByCategoryAndStatus(Category category, boolean status);
    List<Product> findByCreatedAtAfter(LocalDateTime time);
    
    // Top 10 best-selling products (using actual field name 'soldCount')
    List<Product> findTop10ByOrderBySoldCountDesc();
    
    // Alias methods using @Query to avoid property name mismatch
    @Query(value = "SELECT * FROM products ORDER BY sold_count DESC LIMIT 10", nativeQuery = true)
    List<Product> findTop10ByOrderBySalesDesc();
    
    // Lấy top 10 sản phẩm bán chạy nhất theo tên danh mục
    List<Product> findTop10ByCategory_CategoryNameOrderBySoldCountDesc(String categoryName);
    
    @Query(value = "SELECT p.* FROM products p INNER JOIN categories c ON p.categoryid = c.categoryid WHERE c.category_name = :categoryName ORDER BY p.sold_count DESC LIMIT 10", nativeQuery = true)
    List<Product> findTop10ByCategory_CategoryNameOrderBySalesDesc(String categoryName);

    // Xoá ảnh phụ thuộc trước khi xoá product để tránh lỗi ràng buộc FK
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM product_images WHERE product_id = :productId", nativeQuery = true)
    void deleteImagesByProductId(int productId);
}
