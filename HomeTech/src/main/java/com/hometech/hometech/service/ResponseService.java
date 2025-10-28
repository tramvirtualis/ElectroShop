package com.hometech.hometech.service;

import com.hometech.hometech.model.Admin;
import com.hometech.hometech.model.Response;
import com.hometech.hometech.model.Review;
import com.hometech.hometech.Repository.AdminRepository;
import com.hometech.hometech.Repository.ResponseRepository;
import com.hometech.hometech.Repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ResponseService {

    private final ResponseRepository responseRepository;
    private final ReviewRepository reviewRepository;
    private final AdminRepository adminRepository;

    public ResponseService(ResponseRepository responseRepository,
                           ReviewRepository reviewRepository,
                           AdminRepository adminRepository) {
        this.responseRepository = responseRepository;
        this.reviewRepository = reviewRepository;
        this.adminRepository = adminRepository;
    }

    /**
     * 🟢 Admin phản hồi một đánh giá
     */
    public void replyToReview(int reviewId, int adminId, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin"));

        Response response = new Response();
        response.setReview(review);
        response.setAdmin(admin);
        response.setContent(content);
        response.setTimeStamp(new Date());

        responseRepository.save(response);
    }

    /**
     * 🟢 Lấy tất cả phản hồi
     */
    public List<Response> getAllResponses() {
        return responseRepository.findAll();
    }

    /**
     * 🟢 Lấy phản hồi theo review
     */
    public Response getResponseByReview(int reviewId) {
        return responseRepository.findByReview_ReviewID(reviewId);
    }
}
