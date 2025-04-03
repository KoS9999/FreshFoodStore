package com.example.foodstore.controller;

import com.example.foodstore.entity.*;
import com.example.foodstore.repository.ViewLogRepository;
import com.example.foodstore.service.ProductImageService;
import com.example.foodstore.service.ProductService;
import com.example.foodstore.service.ReviewService;
import com.example.foodstore.service.UserService;
import com.example.foodstore.util.CBFUtil;
import com.example.foodstore.util.CFUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/product-details")
public class ProductDetailController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private ViewLogRepository viewLogRepository;

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model, Principal principal) throws Exception {
        Product product = productService.findByProductId(id);
        List<ProductImage> productImages = productImageService.findByProduct(product);
        List<Review> reviews = reviewService.getReviewsWithUserByProduct(id);
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        model.addAttribute("averageRating", averageRating);
        model.addAttribute("reviews", reviews);
        model.addAttribute("product", product);
        model.addAttribute("productImages", productImages);

        User user = null;
        List<Product> recommended = new ArrayList<>();

        if (principal != null) {
            user = userService.getLoggedInUser();
            viewLogRepository.save(new ViewLog(null, user, product, new Date()));

            //Hybrid Recommendation (CBF + CF)
            List<Product> allProducts = productService.findAll();
            List<User> allUsers = userService.findAll();
            List<Review> allReviews = reviewService.findAll();
            List<OrderDetail> allOrders = reviewService.findAllOrderDetails();

            Map<Product, Double> cbfScores = CBFUtil.findSimilarProducts(product, allProducts, 20);
            Map<Product, Double> cfScores = CFUtil.recommendByUserBehavior(user, allUsers, allReviews, allOrders, 20);

            Map<Product, Double> hybridScores = new HashMap<>();
            double alpha = 0.4;

            Set<Product> allKeys = new HashSet<>();
            allKeys.addAll(cbfScores.keySet());
            allKeys.addAll(cfScores.keySet());

            for (Product p : allKeys) {
                double cbf = cbfScores.getOrDefault(p, 0.0);
                double cf = cfScores.getOrDefault(p, 0.0);
                hybridScores.put(p, alpha * cbf + (1 - alpha) * cf);
            }

            recommended = hybridScores.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .map(Map.Entry::getKey)
                    .filter(p -> !p.getProductId().equals(product.getProductId()))
                    .limit(10)
                    .collect(Collectors.toList());

            List<Long> recommendedProductIds = recommended.stream().map(Product::getProductId).collect(Collectors.toList());
            List<Product> recommendedProducts = productService.findProductsByIds(recommendedProductIds);

            model.addAttribute("recommendedProducts", recommendedProducts);
        } else {
            // Người dùng chưa đăng nhập, chỉ gợi ý dựa trên CBF
            List<Product> allProducts = productService.findAll();
            Map<Product, Double> cbfScores = CBFUtil.findSimilarProducts(product, allProducts, 6);
            recommended = new ArrayList<>(cbfScores.keySet());

            List<Long> recommendedProductIds = recommended.stream().map(Product::getProductId).collect(Collectors.toList());
            List<Product> recommendedProducts = productService.findProductsByIds(recommendedProductIds);

            model.addAttribute("recommendedProducts", recommendedProducts);
        }

        System.out.println("Số lượng sản phẩm gợi ý: " + recommended.size());

        Map<Long, Double> averageRatings = calculateAverageRatings(recommended);
        model.addAttribute("averageRatings", averageRatings);

        return "web/product-details";
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