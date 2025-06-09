package com.example.foodstore.controller;

import com.example.foodstore.entity.Blog;
import com.example.foodstore.entity.Category;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.Review;
import com.example.foodstore.service.BlogService;
import com.example.foodstore.service.CategoryService;
import com.example.foodstore.service.ProductService;
import com.example.foodstore.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BlogService blogService;

    @GetMapping({"/", "/index"})
    public String index(@RequestParam(defaultValue = "") String keyword, Model model, HttpSession session) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);

        Map<Long, Double> allAverageRatings = new HashMap<>();

        if (!categories.isEmpty()) {
            for (Category category : categories) {
                List<Product> products = productService.findProductsByCategoryId(category.getCategoryId());
                category.setProducts(products);
                Map<Long, Double> ratingsForCategory = calculateAverageRatings(products);
                allAverageRatings.putAll(ratingsForCategory);
            }

            Long defaultCategoryId = categories.get(0).getCategoryId();
            List<Product> defaultProducts = productService.findProductsByCategoryId(defaultCategoryId);
            model.addAttribute("products", defaultProducts);
        }
        model.addAttribute("averageRatings", allAverageRatings);
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        List<Product> seasonalProducts = productService.getProductsBySeasonMonth(currentMonth);
        model.addAttribute("seasonProducts", seasonalProducts);
        List<Product> newestProducts = productService.findTop8ByOrderByEnteredDateDesc();
        model.addAttribute("newestProducts", newestProducts);
        List<Product> topSellingProducts = productService.getTop8BestSellingProducts();
        model.addAttribute("topSellingProducts", topSellingProducts);
        Map<Long, Double> seasonalRatings = calculateAverageRatings(seasonalProducts);
        allAverageRatings.putAll(seasonalRatings);

        List<Blog> blogs = blogService.getLatestBlogs();
        model.addAttribute("blogs", blogs);

        Integer totalCartItems = (Integer) session.getAttribute("totalCartItems");
        if (totalCartItems == null) {
            totalCartItems = 0;
        }
        model.addAttribute("totalCartItems", totalCartItems);
        return "web/index";
    }

    @GetMapping("/category/{id}")
    public String getProductsByCategoryId(@PathVariable("id") Long categoryId, Model model) {
        List<Product> products = productService.findProductsByCategoryId(categoryId);
        Map<Long, Double> averageRatings = calculateAverageRatings(products);
        model.addAttribute("products", products);
        model.addAttribute("averageRatings", averageRatings);
        return "web/index :: #tab-content";
    }

    @GetMapping("/new-products")
    public String getNewProducts(Model model) {
        List<Product> newestProducts = productService.findTop8ByOrderByEnteredDateDesc();
        Map<Long, Double> averageRatings = calculateAverageRatings(newestProducts);
        model.addAttribute("newestProducts", newestProducts);
        model.addAttribute("averageRatings", averageRatings);
        return "web/index :: #new-products";
    }

    @GetMapping("/top-selling-products")
    public String getTopSellingProducts(Model model) {
        List<Product> topSellingProducts = productService.getTop8BestSellingProducts();
        Map<Long, Double> averageRatings = calculateAverageRatings(topSellingProducts);
        model.addAttribute("topSellingProducts", topSellingProducts);
        model.addAttribute("averageRatings", averageRatings);
        return "web/index :: #top-selling-products";
    }

    @GetMapping("/season-products")
    public String getSeasonProducts(Model model) {
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        List<Product> seasonalProducts = productService.getProductsBySeasonMonth(currentMonth);
        Map<Long, Double> averageRatings = calculateAverageRatings(seasonalProducts);
        model.addAttribute("seasonProducts", seasonalProducts);
        model.addAttribute("averageRatings", averageRatings);
        return "web/index :: #season-products";
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
