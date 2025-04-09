package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.OrderDetail;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.Review;
import com.example.foodstore.entity.User;
import com.example.foodstore.repository.OrderDetailRepository;
import com.example.foodstore.repository.ProductRepository;
import com.example.foodstore.repository.ReviewRepository;
import com.example.foodstore.repository.UserRepository;
import com.example.foodstore.service.FirebaseStorageService;
import com.example.foodstore.service.NotificationService;
import com.example.foodstore.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final NotificationService notificationService;

    @Override
    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductProductId(productId);
    }

    @Override
    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserUserId(userId);
    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    @Override
    public Review createReview(Long userId, Long productId, Long orderDetailId, int rating, String reviewText, MultipartFile image1, MultipartFile image2) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Order Detail not found"));

        List<Review> existingReviews = reviewRepository.findByUserAndOrderDetail(user, orderDetail);
        if (!existingReviews.isEmpty()) {
            throw new RuntimeException("Bạn đã đánh giá lần mua này!");
        }

        String imageUrl1 = (image1 != null && !image1.isEmpty()) ? firebaseStorageService.uploadFile(image1) : null;
        String imageUrl2 = (image2 != null && !image2.isEmpty()) ? firebaseStorageService.uploadFile(image2) : null;

        Review review = new Review(user, product, orderDetail, rating, reviewText, new Date(), null, imageUrl1, imageUrl2);
        review.setVisible(false);
        Review savedReview = reviewRepository.save(review);

        user.setPoints(user.getPoints() + 1000);
        userRepository.save(user);
        notificationService.sendNewReviewNotification("/topic/admin", "Có đánh giá mới từ khách hàng!");

        return savedReview;
    }


    @Override
    public Review updateReview(Long reviewId, int rating, String reviewText, MultipartFile image1, MultipartFile image2, boolean deleteImage1, boolean deleteImage2) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại!"));

        review.setRating(rating);
        review.setReviewText(reviewText);
        review.setReviewDate(new Date());

        if (deleteImage1 && review.getImageUrl1() != null) {
            firebaseStorageService.deleteFile(review.getImageUrl1());
            review.setImageUrl1(null);
        }
        if (deleteImage2 && review.getImageUrl2() != null) {
            firebaseStorageService.deleteFile(review.getImageUrl2());
            review.setImageUrl2(null);
        }

        if (image1 != null && !image1.isEmpty()) {
            review.setImageUrl1(firebaseStorageService.uploadFile(image1));
        }
        if (image2 != null && !image2.isEmpty()) {
            review.setImageUrl2(firebaseStorageService.uploadFile(image2));
        }

        return reviewRepository.save(review);
    }


    @Override
    public Review replyToReview(Long reviewId, String responseText) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại!"));

        review.setResponseText(responseText);
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewsByUserAndOrderDetail(User user, OrderDetail orderDetail) {
        return reviewRepository.findByUserAndOrderDetail(user, orderDetail);
    }

    @Override
    public List<Review> getReviewsWithUserByProduct(Long productId) {
        return reviewRepository.findReviewsWithUserByProduct(productId);
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByReviewDateDesc();
    }


    @Override
    public List<OrderDetail> findAllOrderDetails() {
        return orderDetailRepository.findAll();
    }

    @Override
    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    @Override
    public Review toggleReviewVisibility(Long reviewId, boolean visible) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review không tồn tại!"));

        review.setVisible(visible);
        return reviewRepository.save(review);
    }

}
