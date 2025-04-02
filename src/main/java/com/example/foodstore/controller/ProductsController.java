package com.example.foodstore.controller;

import com.example.foodstore.entity.Category;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.Review;
import com.example.foodstore.service.CategoryService;
import com.example.foodstore.service.ProductService;
import com.example.foodstore.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductsController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/shop")
    public String showShopPage(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") Long categoryId,
            @RequestParam(defaultValue = "0") double minPrice,
            @RequestParam(defaultValue = "1000000") double maxPrice,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        Page<Product> productPage = productService.searchProducts(keyword, categoryId, minPrice, maxPrice, PageRequest.of(page - 1, 6, Sort.by("price").ascending()));
        List<Category> categories = categoryService.findAll();
        List<Product> products = productPage.getContent();

        Map<Long, Double> averageRatings = calculateAverageRatings(products);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("averageRatings", averageRatings);

        return "web/shop";
    }

    private Map<Long, Double> calculateAverageRatings(List<Product> products) {
        Map<Long, Double> averageRatings = new HashMap<>();
        for (Product product : products) {
            List<Review> reviews = reviewService.getReviewsByProduct(product.getProductId());
            double averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            averageRatings.put(product.getProductId(), averageRating);
        }
        return averageRatings;
    }
}
