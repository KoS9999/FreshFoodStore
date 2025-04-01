package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.Review;
import com.example.foodstore.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class ResponseController {

    private final ReviewService reviewService;

    @GetMapping
    public String showReviewList(Model model) {
        List<Review> reviews = reviewService.getAllReviews();
        model.addAttribute("reviews", reviews);
        return "admin/review-list";
    }

    @GetMapping("/api/all")
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/reply/{reviewId}")
    @ResponseBody
    public ResponseEntity<Review> replyToReview(@PathVariable Long reviewId, @RequestParam String responseText) {
        Review updatedReview = reviewService.replyToReview(reviewId, responseText);
        return ResponseEntity.ok(updatedReview);
    }
    @PostMapping("/toggle-visibility/{reviewId}")
    @ResponseBody
    public ResponseEntity<Review> toggleVisibility(@PathVariable Long reviewId, @RequestParam boolean visible) {
        Review updatedReview = reviewService.toggleReviewVisibility(reviewId, visible);
        return ResponseEntity.ok(updatedReview);
    }

}

