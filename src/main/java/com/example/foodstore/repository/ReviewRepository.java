package com.example.foodstore.repository;

import com.example.foodstore.entity.OrderDetail;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.Review;
import com.example.foodstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductProductId(Long productId);
    List<Review> findByUserUserId(Long userId);
    List<Review> findByUserAndOrderDetail(User user, OrderDetail orderDetail);
    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.product.productId = :productId ORDER BY r.reviewDate DESC")
    List<Review> findReviewsWithUserByProduct(Long productId);
    List<Review> findAllByOrderByReviewDateDesc();

}
