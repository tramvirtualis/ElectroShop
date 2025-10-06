package com.electroshop.repository;

import com.electroshop.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByProduct_Id(Long productId);
    
    List<Review> findByCustomer_Id(Long customerId);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.customer.id = :customerId")
    Optional<Review> findByProductAndCustomer(@Param("productId") Long productId, @Param("customerId") Long customerId);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.reviewDate DESC")
    List<Review> findByProductIdOrderByReviewDateDesc(@Param("productId") Long productId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);
    
    @Query("SELECT r FROM Review r WHERE r.rating = :rating")
    List<Review> findByRating(@Param("rating") Integer rating);
    
    @Query("SELECT r FROM Review r WHERE r.product.shop.id = :shopId")
    List<Review> findByShopId(@Param("shopId") Long shopId);
    
    @Query("SELECT r.product.id, AVG(r.rating) FROM Review r GROUP BY r.product.id HAVING AVG(r.rating) >= :minRating ORDER BY AVG(r.rating) DESC")
    List<Object[]> findProductsWithRatingAbove(@Param("minRating") Double minRating);
}


