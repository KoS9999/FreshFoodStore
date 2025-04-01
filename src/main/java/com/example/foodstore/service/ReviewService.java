package com.example.foodstore.service;

import com.example.foodstore.entity.OrderDetail;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.Review;
import com.example.foodstore.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    List<Review> getReviewsByProduct(Long productId);
    List<Review> getReviewsByUser(Long userId);
    Optional<Review> getReviewById(Long id);
    Review createReview(Long userId, Long productId, Long orderDetailId, int rating, String reviewText, MultipartFile image1, MultipartFile image2);
    Review updateReview(Long reviewId, int rating, String reviewText, MultipartFile image1, MultipartFile image2, boolean deleteImage1, boolean deleteImage2);
    Review replyToReview(Long reviewId, String responseText);
    List<Review> getReviewsByUserAndOrderDetail(User user, OrderDetail orderDetail);
    List<Review> getReviewsWithUserByProduct(Long productId);
    List<Review> getAllReviews();
    List<OrderDetail> findAllOrderDetails();
    List<Review> findAll();
    Review toggleReviewVisibility(Long reviewId, boolean visible);
}
