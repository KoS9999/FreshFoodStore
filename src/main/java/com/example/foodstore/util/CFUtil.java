package com.example.foodstore.util;

import com.example.foodstore.entity.OrderDetail;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.Review;
import com.example.foodstore.entity.User;

import java.util.*;
import java.util.stream.Collectors;

public class CFUtil {

    public static Map<Product, Double> recommendByUserBehavior(User currentUser,
                                                               List<User> allUsers,
                                                               List<Review> allReviews,
                                                               List<OrderDetail> allOrders,
                                                               int topN) {
        System.out.println(" [CF] Current User: " + currentUser.getName());

        // Xây dựng ma trận người dùng - sản phẩm
        Map<Long, Map<Long, Double>> userProductMatrix = new HashMap<>();

        for (OrderDetail od : allOrders) {
            Long uid = od.getOrder().getUser().getUserId();
            Long pid = od.getProduct().getProductId();
            userProductMatrix.computeIfAbsent(uid, k -> new HashMap<>()).put(pid, 3.0);
            System.out.println(" [CF] Order - User " + uid + " bought Product " + pid + ", default rating 3.0");
        }

        for (Review r : allReviews) {
            Long uid = r.getUser().getUserId();
            Long pid = r.getProduct().getProductId();
            double rating = r.getRating();
            userProductMatrix.computeIfAbsent(uid, k -> new HashMap<>()).put(pid, rating);
            System.out.println(" [CF] Review - User " + uid + " rated Product " + pid + " with " + rating);
        }

        Map<Long, Double> currentRatings = userProductMatrix.get(currentUser.getUserId());
        if (currentRatings == null || currentRatings.isEmpty()) {
            System.out.println(" [CF] No ratings for current user");
            return new HashMap<>();
        }

        Map<User, Double> similarityMap = new HashMap<>();
        for (User other : allUsers) {
            if (other.getUserId().equals(currentUser.getUserId())) continue;

            Map<Long, Double> otherRatings = userProductMatrix.get(other.getUserId());
            if (otherRatings == null || otherRatings.isEmpty()) continue;

            double similarity = cosineSimilarity(currentRatings, otherRatings);
            if (similarity > 0) {
                similarityMap.put(other, similarity);
                System.out.println(" [CF] Similarity to user " + other.getName() + ": " + similarity);
            }
        }

        Map<Product, Double> recommendations = new HashMap<>();
        for (Map.Entry<User, Double> entry : similarityMap.entrySet()) {
            User similarUser = entry.getKey();
            double similarity = entry.getValue();
            Map<Long, Double> otherRatings = userProductMatrix.get(similarUser.getUserId());

            for (Map.Entry<Long, Double> ratingEntry : otherRatings.entrySet()) {
                Long productId = ratingEntry.getKey();

                if (!currentRatings.containsKey(productId)) {
                    Product p = new Product();
                    p.setProductId(productId);
                    double score = similarity * ratingEntry.getValue();

                    Optional<Review> review = allReviews.stream()
                            .filter(r -> r.getProduct().getProductId().equals(productId))
                            .max(Comparator.comparingDouble(Review::getRating));

                    if (review.isPresent()) {
                        score += review.get().getRating() * 0.2;
                    }

                    recommendations.merge(p, score, Double::sum);
                    System.out.println(" [CF] Candidate Product " + productId + " score: " + score);
                }
            }
        }

        System.out.println(" [CF] Number of CF recommendations: " + recommendations.size());
        return recommendations.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
    }

    private static double cosineSimilarity(Map<Long, Double> v1, Map<Long, Double> v2) {
        Set<Long> allKeys = new HashSet<>(v1.keySet());
        allKeys.addAll(v2.keySet());
        double dot = 0, mag1 = 0, mag2 = 0;
        for (Long k : allKeys) {
            double a = v1.getOrDefault(k, 0.0);
            double b = v2.getOrDefault(k, 0.0);
            dot += a * b;
            mag1 += a * a;
            mag2 += b * b;
        }
        return (mag1 == 0 || mag2 == 0) ? 0 : dot / (Math.sqrt(mag1) * Math.sqrt(mag2));
    }
}
