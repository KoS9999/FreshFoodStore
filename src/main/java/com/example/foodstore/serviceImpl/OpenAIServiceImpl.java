package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Blog;
import com.example.foodstore.entity.Product;
import com.example.foodstore.repository.BlogRepository;
import com.example.foodstore.repository.ProductRepository;
import com.example.foodstore.service.OpenAIService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIServiceImpl.class);

    @Value("${openai.api.key}")
    private String apiKey;

    private final ProductRepository productRepository;
    private final BlogRepository blogRepository;
    private final RestTemplate restTemplate;

    // Cache cho câu trả lời, hết hạn sau 1 ngày, tối đa 1000 mục
    private final Cache<String, String> responseCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(1000)
            .build();

    // Từ khóa để phân loại câu hỏi
    private static final List<String> PURCHASE_KEYWORDS = Arrays.asList("mua", "bán", "đặt", "giá", "thanh toán", "giỏ hàng");
    private static final List<String> AVAILABILITY_KEYWORDS = Arrays.asList("có bán", "có kinh doanh", "có không");
    private static final List<String> USAGE_KEYWORDS = Arrays.asList("công dụng", "tác dụng", "lợi ích", "dinh dưỡng");
    private static final List<String> RECIPE_KEYWORDS = Arrays.asList("món", "nấu", "kết hợp", "cách làm", "làm","công thức");
    private static final List<String> OPEN_QUESTION_INDICATORS = Arrays.asList(
            "bà bầu", "phụ nữ mang thai", "trẻ em", "trẻ nhỏ", "người già", "bệnh", "tiểu đường",
            "huyết áp", "tim mạch", "giảm cân", "tăng cân", "sức khỏe", "phù hợp", "tốt cho",
            "mắt", "da", "tóc", "xương", "ruột","miễn dịch", "tiêu hóa", "vitamin", "khoáng chất"
    );
    private static final List<String> NON_FOOD_KEYWORDS = Arrays.asList("xe máy", "điện thoại", "quần áo", "giày dép");

    public OpenAIServiceImpl(ProductRepository productRepository, BlogRepository blogRepository) {
        this.productRepository = productRepository;
        this.blogRepository = blogRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getChatBotReply(String userMessage) {
        // Chuẩn hóa câu hỏi
        String normalizedQuery = normalizeText(userMessage);
        log.info("Processing query: {}", userMessage);

        // Trích xuất tên sản phẩm
        String productName = extractProductName(normalizedQuery);
        log.info("Extracted productName: {}", productName);

        // Kiểm tra câu hỏi không liên quan
        if (isClearlyNonFoodRelated(normalizedQuery) && productName == null) {
            return buildFriendlyReply(
                    "🤖 Xin lỗi, mình chỉ hỗ trợ các nội dung liên quan đến thực phẩm sạch, sản phẩm, món ăn hoặc dinh dưỡng. " +
                            "Bạn có thể thử hỏi: 'Cách nấu bí đỏ', 'Rau nào tốt cho sức khỏe?', hoặc 'Mua khoai tây ở đâu?' 🌱",
                    null
            );
        }

        // Phân loại và xử lý câu hỏi
        try {
            String reply = classifyAndHandleQuery(normalizedQuery, productName, userMessage);
            return reply != null && !reply.isEmpty() ? reply : fetchChatGPTResponse(userMessage);
        } catch (Exception e) {
            log.error("Error processing query '{}': {}", userMessage, e.getMessage());
            return buildFriendlyReply("⚠️ Xin lỗi, hệ thống đang gặp sự cố. Vui lòng thử lại sau!", null);
        }
    }

    private String classifyAndHandleQuery(String normalizedQuery, String productName, String originalQuery) {
        // Xác định loại câu hỏi
        QueryType queryType = determineQueryType(normalizedQuery, productName);
        log.info("Query type: {}", queryType);

        switch (queryType) {
            case PURCHASE:
                return handlePurchaseQuery(normalizedQuery, productName);
            case AVAILABILITY:
                return handleAvailabilityQuery(productName);
            case OPEN_QUESTION:
                return handleOpenQuestion(originalQuery, productName);
            case USAGE:
                return handleUsageQuery(normalizedQuery, productName);
            case RECIPE:
                return handleRecipeQuery(normalizedQuery, productName);
            case GENERAL_PRODUCT:
                return handleGeneralProductQuery(productName);
            default:
                return "";
        }
    }

    private enum QueryType {
        PURCHASE, AVAILABILITY, OPEN_QUESTION, USAGE, RECIPE, GENERAL_PRODUCT, UNKNOWN
    }

    private QueryType determineQueryType(String normalizedQuery, String productName) {
        String cacheKey = "query_type:" + normalizedQuery;
        String cachedType = responseCache.getIfPresent(cacheKey);
        if (cachedType != null) {
            return QueryType.valueOf(cachedType);
        }

        if (PURCHASE_KEYWORDS.stream().anyMatch(normalizedQuery::contains)) {
            responseCache.put(cacheKey, QueryType.PURCHASE.name());
            return QueryType.PURCHASE;
        }
        if (productName != null && AVAILABILITY_KEYWORDS.stream().anyMatch(normalizedQuery::contains)) {
            responseCache.put(cacheKey, QueryType.AVAILABILITY.name());
            return QueryType.AVAILABILITY;
        }
        if (OPEN_QUESTION_INDICATORS.stream().anyMatch(normalizedQuery::contains)) {
            responseCache.put(cacheKey, QueryType.OPEN_QUESTION.name());
            return QueryType.OPEN_QUESTION;
        }
        if (USAGE_KEYWORDS.stream().anyMatch(normalizedQuery::contains)) {
            responseCache.put(cacheKey, QueryType.USAGE.name());
            return QueryType.USAGE;
        }
        if (RECIPE_KEYWORDS.stream().anyMatch(normalizedQuery::contains)) {
            responseCache.put(cacheKey, QueryType.RECIPE.name());
            return QueryType.RECIPE;
        }
        if (productName != null) {
            responseCache.put(cacheKey, QueryType.GENERAL_PRODUCT.name());
            return QueryType.GENERAL_PRODUCT;
        }

        responseCache.put(cacheKey, QueryType.UNKNOWN.name());
        return QueryType.UNKNOWN;
    }

    private String handlePurchaseQuery(String normalizedQuery, String productName) {
        if (productName != null) {
            Optional<Product> product = findProduct(productName);
            if (product.isPresent()) {
                Product p = product.get();
                return buildFriendlyReply(
                        "Chào bạn! Website hiện có bán sản phẩm <strong>" + p.getProductName() + "</strong>.",
                        "Xem chi tiết tại <a href='http://localhost:9090/product-details/" + p.getProductId() + "' target='_blank'>đây</a>."
                );
            }
            return buildFriendlyReply("📌 Hiện tại website chưa kinh doanh sản phẩm <strong>" + productName + "</strong>.", null);
        }
        return buildFriendlyReply(
                "Để mua sản phẩm trên website, bạn vui lòng làm theo các bước sau:",
                "<ol>" +
                        "<li><strong>Đăng nhập tài khoản</strong></li>" +
                        "<li><strong>Chọn sản phẩm và cho vào giỏ hàng</strong></li>" +
                        "<li><strong>Vào giỏ hàng và ấn 'Thanh toán'</strong></li>" +
                        "<li><strong>Điền đầy đủ thông tin, kiểm tra đơn hàng và áp dụng khuyến mãi (nếu có)</strong></li>" +
                        "<li><strong>Chọn phương thức thanh toán (ZaloPay, VNPay hoặc COD)</strong></li>" +
                        "<li><strong>Sau khi hoàn tất, bạn sẽ nhận được email xác nhận đơn đặt hàng</strong></li>" +
                        "</ol>" +
                        "Tham khảo chi tiết tại <a href='http://localhost:9090/shop' target='_blank'>liên kết này</a>."
        );
    }

    private String handleAvailabilityQuery(String productName) {
        Optional<Product> product = findProduct(productName);
        if (product.isPresent()) {
            Product p = product.get();
            return buildFriendlyReply(
                    "Chào bạn! Website hiện có bán sản phẩm <strong>" + p.getProductName() + "</strong>.",
                    "Xem chi tiết tại <a href='http://localhost:9090/product-details/" + p.getProductId() + "' target='_blank'>đây</a>."
            );
        }
        return buildFriendlyReply("📌 Hiện tại website chưa kinh doanh sản phẩm <strong>" + productName + "</strong>.", null);
    }

    private String handleOpenQuestion(String originalQuery, String productName) {
        String gptResponse = fetchChatGPTResponseForOpenQuestion(originalQuery, productName);
        return buildFriendlyReply("Thông tin về " + (productName != null ? productName : "thực phẩm"), gptResponse);
    }

    private String handleUsageQuery(String normalizedQuery, String productName) {
        if (productName == null) {
            return fetchChatGPTResponse("Liệt kê các công dụng chung của thực phẩm sạch đối với sức khỏe.");
        }
        Optional<Product> product = findProduct(productName);
        if (product.isPresent()) {
            Product p = product.get();
            String usageInfo = p.getDescription();
            if (usageInfo != null && !usageInfo.isEmpty()) {
                log.info("Retrieved usageInfo for {} from database", p.getProductName());
                return buildFriendlyReply("Công dụng của <strong>" + p.getProductName() + "</strong>", usageInfo);
            }
        }
        String gptUsage = fetchChatGPTUsage(productName);
        if (product.isPresent()) {
            Product p = product.get();
            p.setDescription(gptUsage);
            try {
                productRepository.save(p);
                log.info("Saved usageInfo for {} to database", p.getProductName());
            } catch (Exception e) {
                log.error("Failed to save usageInfo for {}: {}", p.getProductName(), e.getMessage());
            }
        }
        return buildFriendlyReply("Công dụng của <strong>" + productName + "</strong>", gptUsage);
    }

    private String handleRecipeQuery(String normalizedQuery, String productName) {
        String dishName = extractDishName(normalizedQuery);
        if (dishName != null) {
            List<Blog> blogs = blogRepository.findAll().stream()
                    .filter(b -> normalizeText(b.getMarkdownContent() + " " + b.getTitle()).contains(dishName))
                    .collect(Collectors.toList());

            if (!blogs.isEmpty()) {
                String links = blogs.stream()
                        .map(b -> "• <a href='http://localhost:9090/news/" + b.getSlug() + "' target='_blank'>" + b.getTitle() + "</a>")
                        .collect(Collectors.joining("<br>"));
                log.info("Found {} relevant blogs for dish: {}", blogs.size(), dishName);
                return buildFriendlyReply("🥗 Tham khảo món ngon liên quan:", links);
            }
        }

        String recipeTarget = dishName != null ? dishName : productName;
        if (recipeTarget != null) {
            String recipe = fetchChatGPTRecipe(recipeTarget);
            return buildFriendlyReply("Cách làm món <strong>" + recipeTarget + "</strong>", recipe);
        }
        return "";
    }

    private String handleGeneralProductQuery(String productName) {
        Optional<Product> product = findProduct(productName);
        if (product.isPresent()) {
            Product p = product.get();
            return buildFriendlyReply(
                    "Thông tin sản phẩm <strong>" + p.getProductName() + "</strong>",
                    p.getDescription() + "<br>Xem chi tiết tại <a href='http://localhost:9090/product-details/" + p.getProductId() + "' target='_blank'>liên kết này</a>."
            );
        }
        return buildFriendlyReply("📌 Hiện tại website chưa kinh doanh sản phẩm <strong>" + productName + "</strong>.", null);
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        return text.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private boolean isClearlyNonFoodRelated(String normalizedQuery) {
        return NON_FOOD_KEYWORDS.stream().anyMatch(normalizedQuery::contains);
    }

    private String extractProductName(String normalizedQuery) {
        String cacheKey = "product_name:" + normalizedQuery;
        String cachedName = responseCache.getIfPresent(cacheKey);
        if (cachedName != null) {
            return cachedName.equals("null") ? null : cachedName;
        }

        // Kiểm tra trong DB
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            String productName = normalizeText(product.getProductName());
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(productName) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(normalizedQuery).find()) {
                responseCache.put(cacheKey, product.getProductName());
                return product.getProductName();
            }
        }

        // Gọi ChatGPT
        String prompt = "Trích xuất chính xác tên sản phẩm thực phẩm (rau, củ, quả, cá, thịt) bằng tiếng Việt có dấu từ câu sau. " +
                "Chỉ trả về tên sản phẩm, không thêm từ nào. Nếu không có sản phẩm, trả về 'null'. Câu: \"" + normalizedQuery + "\"";
        try {
            String result = sendToChatGPT(prompt).trim();
            responseCache.put(cacheKey, result);
            return result.equals("null") ? null : result;
        } catch (Exception e) {
            log.error("Error extracting product name from GPT: {}", e.getMessage());
            return null;
        }
    }

    private String extractDishName(String normalizedQuery) {
        String cacheKey = "dish_name:" + normalizedQuery;
        String cachedDish = responseCache.getIfPresent(cacheKey);
        if (cachedDish != null) {
            return cachedDish.equals("null") ? null : cachedDish;
        }

        String[] dishPatterns = {"bún xào chay", "món chay", "salad", "nước ép", "sinh tố", "cá kho", "phở chay", "gỏi cuốn", "chả giò chay"};
        for (String pattern : dishPatterns) {
            if (normalizedQuery.contains(pattern)) {
                responseCache.put(cacheKey, pattern);
                return pattern;
            }
        }

        String prompt = "Trích xuất tên món ăn bằng tiếng Việt từ câu sau, chỉ trả về tên món ăn hoặc 'null' nếu không có. " +
                "Ví dụ: 'cách làm cá kho' -> 'cá kho'. Câu: \"" + normalizedQuery + "\"";
        String dishName = sendToChatGPT(prompt).trim();
        responseCache.put(cacheKey, dishName);
        return dishName.equals("null") ? null : dishName;
    }

    private Optional<Product> findProduct(String productName) {
        String normalizedProductName = normalizeText(productName);
        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            String dbName = normalizeText(p.getProductName());
            if (dbName.equals(normalizedProductName)) {
                return Optional.of(p);
            }
        }
        for (Product p : products) {
            String dbName = normalizeText(p.getProductName());
            if ((dbName.contains(normalizedProductName) || normalizedProductName.contains(dbName)) && normalizedProductName.length() > 4) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    private String sendToChatGPT(String prompt) {
        String cacheKey = "gpt_response:" + normalizeText(prompt);
        String cachedResponse = responseCache.getIfPresent(cacheKey);
        if (cachedResponse != null) {
            log.info("Retrieved GPT response from cache for prompt: {}", prompt);
            return cachedResponse;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions", entity, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = message.get("content").toString();
        responseCache.put(cacheKey, content);
        log.info("Fetched GPT response for prompt: {}", prompt);
        return content;
    }

    private String fetchChatGPTResponse(String query) {
        String prompt = "Trả lời ngắn gọn và chính xác bằng tiếng Việt câu hỏi sau liên quan đến thực phẩm: '" + query + "'. " +
                "Câu trả lời phải phù hợp với ngữ cảnh và tập trung vào chủ đề thực phẩm sạch, dinh dưỡng hoặc món ăn.";
        return formatResponse(sendToChatGPT(prompt));
    }

    private String fetchChatGPTResponseForOpenQuestion(String query, String productName) {
        String prompt = "Trả lời ngắn gọn và chính xác bằng tiếng Việt câu hỏi sau: '" + query + "'. " +
                "Câu trả lời phải tập trung vào '" + (productName != null ? productName : "thực phẩm") + "' và ngữ cảnh cụ thể (ví dụ: sức khỏe, đối tượng như bà bầu, trẻ em).";
        return formatResponse(sendToChatGPT(prompt));
    }

    private String fetchChatGPTUsage(String productName) {
        String prompt = "Liệt kê ngắn gọn các công dụng của '" + productName + "' đối với sức khỏe bằng tiếng Việt.";
        return formatResponse(sendToChatGPT(prompt));
    }

    private String fetchChatGPTRecipe(String target) {
        String prompt = "Gợi ý một món ăn đơn giản có nguyên liệu chính là '" + target + "'. " +
                "Viết ngắn gọn bằng tiếng Việt, liệt kê nguyên liệu và các bước thực hiện.";
        return formatResponse(sendToChatGPT(prompt));
    }

    private String formatResponse(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Không có thông tin để hiển thị.";
        }
        return content.replaceAll("\n", "<br>")
                .replaceAll("\\. ", ".<br>");
    }

    private String buildFriendlyReply(String title, String content) {
        StringBuilder reply = new StringBuilder();
        reply.append("<p>").append(title).append("</p>");
        if (content != null && !content.isEmpty()) {
            reply.append("<p><em>").append(content).append("</em></p>");
        }
        return reply.toString();
    }
}