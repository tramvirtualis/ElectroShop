package com.hometech.hometech.Repository;

import com.hometech.hometech.model.Category;
import com.hometech.hometech.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategory(Category category);
    List<Product> findByCategory_CategoryID(int categoryId);
    List<Product> findByCategory_CategoryName(String categoryName);
    List<Product> findByCategoryAndStatus(Category category, boolean status);
    List<Product> findByCreatedAtAfter(LocalDateTime time);
    List<Product> findTop10ByOrderBySoldCountDesc();
}
