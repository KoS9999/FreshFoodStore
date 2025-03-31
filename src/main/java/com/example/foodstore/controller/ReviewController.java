package com.example.foodstore.controller;

import com.example.foodstore.entity.OrderDetail;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.Review;
import com.example.foodstore.entity.User;
import com.example.foodstore.repository.OrderDetailRepository;
import com.example.foodstore.repository.ProductRepository;
import com.example.foodstore.service.ReviewService;
import com.example.foodstore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

    @GetMapping("/product/{productId}")
    public List<Review> getReviewsByProduct(@PathVariable Long productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    @GetMapping("/user/{userId}")
    public List<Review> getReviewsByUser(@PathVariable Long userId) {
        return reviewService.getReviewsByUser(userId);
    }

    @GetMapping("/create")
    public String createReviewPage(@RequestParam("productId") Long productId,
                                   @RequestParam("orderDetailId") Long orderDetailId,
                                   Model model) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            throw new RuntimeException("Người dùng chưa đăng nhập!");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Chi tiết đơn hàng không tồn tại!"));

        model.addAttribute("user", user);
        model.addAttribute("product", product);
        model.addAttribute("orderDetail", orderDetail);
        model.addAttribute("review", new Review());

        return "web/reviewform";
    }
    @GetMapping("/edit")
    public String editReviewPage(@RequestParam("reviewId") Long reviewId, Model model) {
        Review review = reviewService.getReviewById(reviewId)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại!"));

        model.addAttribute("review", review);
        return "web/editReview";
    }
    @PutMapping("/update")
    public ResponseEntity<?> updateReview(
            @RequestParam("reviewId") Long reviewId,
            @RequestParam("rating") int rating,
            @RequestParam("reviewText") String reviewText,
            @RequestParam(value = "image1", required = false) MultipartFile image1,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam(value = "deleteImage1", required = false, defaultValue = "false") boolean deleteImage1,
            @RequestParam(value = "deleteImage2", required = false, defaultValue = "false") boolean deleteImage2
    ) {
        try {
            Review updatedReview = reviewService.updateReview(reviewId, rating, reviewText, image1, image2, deleteImage1, deleteImage2);
            return ResponseEntity.ok().body(Map.of("message", "Review updated successfully", "review", updatedReview));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveReview(@RequestParam("userId") Long userId,
                                        @RequestParam("productId") Long productId,
                                        @RequestParam("orderDetailId") Long orderDetailId,
                                        @RequestParam("rating") int rating,
                                        @RequestParam("reviewText") String reviewText,
                                        @RequestParam(value = "image1", required = false) MultipartFile image1,
                                        @RequestParam(value = "image2", required = false) MultipartFile image2) {
        try {
            Review review = reviewService.createReview(userId, productId, orderDetailId, rating, reviewText, image1, image2);
            return ResponseEntity.ok().body(Map.of("message", "Review saved successfully", "review", review));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
