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

    // 🟢 Kiểm tra xem 1 khách hàng đã đánh giá 1 sản phẩm chưa
    Optional<Review> findByCustomerAndProduct(Customer customer, Product product);

    // 🟢 Lấy tất cả đánh giá (kể cả ẩn)
    List<Review> findByProduct(Product product);

    // 🟢 Lấy tất cả đánh giá chưa ẩn (hiển thị cho người dùng)
    List<Review> findByProductAndHiddenFalse(Product product);

    // 🆕 Lấy đánh giá theo productID (ẩn/không ẩn tùy nhu cầu)
    List<Review> findByProduct_ProductID(int productID);
    List<Review> findByProduct_ProductIDAndHiddenFalse(int productID);

    // 🟢 Tính trung bình rating chỉ trên các đánh giá không bị ẩn
    @Query("SELECT AVG(r.ratingValue) FROM Review r WHERE r.product = :product AND r.hidden = false")
    Double getAverageRatingByProduct(@Param("product") Product product);

    // 🆕 Tính trung bình theo productID
    @Query("SELECT AVG(r.ratingValue) FROM Review r WHERE r.product.productID = :productID AND r.hidden = false")
    Double getAverageRatingByProductId(@Param("productID") int productID);
}
