package com.hometech.hometech.service;


import com.hometech.hometech.model.*;
import com.hometech.hometech.Repository.*;
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        Customer customer = customerRepository.findById(userID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Review review = reviewRepository.findByCustomerAndProduct(customer, product)
                .map(existing -> {
                    existing.setRatingValue(ratingValue);
                    existing.setComment(comment);
                    existing.setUpdatedAt(LocalDateTime.now());
                    if (imageFile != null && !imageFile.isEmpty()) {
                        try {
                            existing.setImage(imageFile.getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException("Không thể đọc ảnh tải lên");
                        }
                    }
                    return reviewRepository.save(existing);
                })
                .orElseGet(() -> {
                    Review newReview = new Review();
                    newReview.setRatingValue(ratingValue);
                    newReview.setComment(comment);
                    newReview.setCreatedAt(LocalDateTime.now());
                    newReview.setUpdatedAt(LocalDateTime.now());
                    newReview.setProduct(product);
                    newReview.setCustomer(customer);
                    if (imageFile != null && !imageFile.isEmpty()) {
                        try {
                            newReview.setImage(imageFile.getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException("Không thể đọc ảnh tải lên");
                        }
                    }
                    return reviewRepository.save(newReview);
                });

        return review;
    }

    // 🟢 Lấy tất cả review của sản phẩm
    public List<Review> getReviewsByProduct(int productID) {
        Product product = productRepository.findById(productID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        return reviewRepository.findByProduct(product);
    }

    // 🟢 Tính trung bình sao (không lưu DB)
    public double getAverageRating(int productID) {
        Product product = productRepository.findById(productID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        Double avg = reviewRepository.getAverageRatingByProduct(product);
        return avg != null ? avg : 0.0;
    }
}
