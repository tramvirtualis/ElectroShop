package com.electroshop.repository;

import com.electroshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByCategory_Id(Long categoryId);
    
    List<Product> findByShop_Id(Long shopId);
    
    List<Product> findByProductStatus(Product.EnumProduct status);
    
    @Query("SELECT p FROM Product p WHERE p.productName LIKE %:name%")
    List<Product> findByProductNameContaining(@Param("name") String name);
    
    @Query("SELECT p FROM Product p WHERE p.productPrice BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE p.productStatus = 'SHOW' ORDER BY p.createdAt DESC")
    List<Product> findVisibleProductsOrderByCreatedAt();
    
    @Query("SELECT p FROM Product p WHERE p.productStatus = 'SHOW' AND p.stockQuantity > 0")
    List<Product> findAvailableProducts();
    
    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.productStatus = :status")
    List<Product> findByShopAndStatus(@Param("shopId") Long shopId, @Param("status") Product.EnumProduct status);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.productStatus = 'SHOW'")
    Page<Product> findVisibleByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.productStatus = 'SHOW'")
    long countVisibleProducts();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId")
    long countByShopId(@Param("shopId") Long shopId);
}


