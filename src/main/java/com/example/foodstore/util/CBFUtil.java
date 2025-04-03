package com.example.foodstore.util;

import com.example.foodstore.entity.Product;
import org.apache.commons.text.similarity.CosineSimilarity;

import java.util.*;

public class CBFUtil {

    public static Map<Product, Double> findSimilarProducts(Product target, List<Product> allProducts, int topN) {
        System.out.println(" [CBF] Target Product: " + target.getProductName());
        System.out.println(" [CBF] Target Product - Description: " + target.getDescription());
        System.out.println(" [CBF] Target Product - Category: " + target.getCategory().getCategoryName());

        // Tạo Map để nhóm sản phẩm theo category
        Map<String, List<Product>> categoryMap = new HashMap<>();
        for (Product product : allProducts) {
            categoryMap.computeIfAbsent(product.getCategory().getCategoryName(), k -> new ArrayList<>()).add(product);
        }

        String targetDescription = target.getDescription();
        String targetProductName = target.getProductName();
        Map<Product, Double> result = new HashMap<>();

        // Duyệt qua từng category và tính toán độ tương đồng
        for (String category : categoryMap.keySet()) {
            List<Product> categoryProducts = categoryMap.get(category);
            for (Product product : categoryProducts) {
                if (product.getProductId().equals(target.getProductId())) continue;

                String description = product.getDescription();
                String productName = product.getProductName();

                double nameSimilarity = calculateCosineSimilarity(targetProductName, productName) * 0.2;
                double descriptionSimilarity = calculateCosineSimilarity(targetDescription, description) * 0.2;

                double categorySimilarity = 0.0;
                if (product.getCategory().getCategoryName().equals(target.getCategory().getCategoryName())) {
                    categorySimilarity = 0.6;
                    System.out.println("🟢 [CBF] Same category - Increasing score");
                }

                double totalScore = nameSimilarity + descriptionSimilarity + categorySimilarity;

                System.out.println("[CBF] Similarity score to " + product.getProductName() + ": " + totalScore);

                if (totalScore > 0) {
                    result.put(product, totalScore);
                }
            }
        }

        Map<Product, Double> sortedRecommendations = result.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

        System.out.println("[CBF] Number of similar products recommended: " + sortedRecommendations.size());

        return sortedRecommendations;
    }



    private static double calculateCosineSimilarity(String targetDescription, String description) {
        CosineSimilarity cosineSimilarity = new CosineSimilarity();

        Map<CharSequence, Integer> targetMap = textToVector(targetDescription);
        Map<CharSequence, Integer> docMap = textToVector(description);

        return cosineSimilarity.cosineSimilarity(targetMap, docMap);
    }


    private static Map<CharSequence, Integer> textToVector(String text) {
        Map<CharSequence, Integer> vector = new HashMap<>();
        String[] words = text.split("\\s+");

        for (String word : words) {
            word = word.toLowerCase().replaceAll("[^a-zA-Z]", "");
            vector.put(word, vector.getOrDefault(word, 0) + 1);
        }

        return vector;
    }

}
