package com.hometech.hometech.Repository;

import com.hometech.hometech.model.Review;
import com.hometech.hometech.model.Product;
import com.hometech.hometech.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Optional<Review> findByCustomerAndProduct(Customer customer, Product product);
    List<Review> findByProduct(Product product);
    @Query("SELECT AVG(r.ratingValue) FROM Review r WHERE r.product = :product")
    Double getAverageRatingByProduct(@Param("product") Product product);
}
