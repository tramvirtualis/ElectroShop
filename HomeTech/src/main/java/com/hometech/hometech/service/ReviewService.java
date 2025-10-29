package com.hometech.hometech.service;

import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.Product;
import com.hometech.hometech.model.Review;
import com.hometech.hometech.Repository.CustomerRepository;
import com.hometech.hometech.Repository.ProductRepository;
import com.hometech.hometech.Repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         CustomerRepository customerRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    // 🟢 Thêm hoặc cập nhật đánh giá
    public Review addOrUpdateReview(int productID, int userID, int ratingValue, String comment, MultipartFile imageFile) {
        Product product = productRepository.findById(productID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productID));

        Customer customer = customerRepository.findById(userID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userID));

        Review review = reviewRepository.findByCustomerAndProduct(customer, product).orElse(null);
        if (review == null) {
            review = new Review();
            review.setProduct(product);
            review.setCustomer(customer);
            review.setCreatedAt(LocalDateTime.now());
        }

        review.setRatingValue(ratingValue);
        review.setComment(comment);
        review.setUpdatedAt(LocalDateTime.now());

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                review.setImage(imageFile.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Không thể đọc ảnh tải lên: " + e.getMessage());
            }
        }

        return reviewRepository.save(review);
    }

    // 🟢 Review cho người dùng (chỉ hiển thị review chưa bị ẩn)
    public List<Review> getVisibleReviewsByProduct(int productID) {
        Product product = productRepository.findById(productID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productID));
        return reviewRepository.findByProductAndHiddenFalse(product);
    }

    // 🟢 Review cho admin (hiển thị tất cả, kể cả ẩn)
    public List<Review> getAllReviewsByProduct(int productID) {
        Product product = productRepository.findById(productID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productID));
        return reviewRepository.findByProduct(product);
    }

    // 🟢 Tính trung bình số sao
    public double getAverageRating(int productID) {
        Product product = productRepository.findById(productID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productID));
        Double avg = reviewRepository.getAverageRatingByProduct(product);
        return avg != null ? avg : 0.0;
    }

    // === API as requested ===
    public List<Review> findByProductId(int productId) {
        return reviewRepository.findByProduct_ProductIDAndHiddenFalse(productId);
    }

    public Double calculateAverageRating(int productId) {
        Double avg = reviewRepository.getAverageRatingByProductId(productId);
        return avg != null ? avg : 0.0;
    }

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    // 🟢 Admin xem tất cả review trong hệ thống
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // 🟢 Xem 1 review cụ thể
    public Review getReviewById(int reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá với ID: " + reviewId));
    }

    // 🟢 Ẩn review
    public void hideReviewById(int reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        review.setHidden(true);
        reviewRepository.save(review);
    }

    // 🟢 Hiện lại review
    public void unhideReviewById(int reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        review.setHidden(false);
        reviewRepository.save(review);
    }
}
