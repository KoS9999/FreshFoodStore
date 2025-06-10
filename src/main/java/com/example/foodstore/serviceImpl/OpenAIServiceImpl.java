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
import org.springframework.http.*;
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

    private final Cache<String, String> gptCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(1000)
            .build();

    private static final List<String> PURCHASE_KEYWORDS = Arrays.asList("mua", "bán", "đặt", "giá", "thanh toán", "giỏ hàng");
    private static final List<String> AVAILABILITY_KEYWORDS = Arrays.asList("có bán", "có kinh doanh", "có không");
    private static final List<String> USAGE_KEYWORDS = Arrays.asList("công dụng", "tác dụng", "lợi ích", "dinh dưỡng");
    private static final List<String> RECIPE_KEYWORDS = Arrays.asList("món", "nấu", "kết hợp", "cách làm", "công thức");

    public OpenAIServiceImpl(ProductRepository productRepository, BlogRepository blogRepository) {
        this.productRepository = productRepository;
        this.blogRepository = blogRepository;
    }

    @Override
    public String getChatBotReply(String userMessage) {
        String productName = extractProductNameLocally(userMessage);
        if (productName == null) {
            productName = extractProductNameFromGPT(userMessage);
        }

        if (!isRelevantToFood(userMessage) && productName == null) {
            return "🤖 Xin lỗi, mình chỉ hỗ trợ các nội dung liên quan đến thực phẩm sạch, sản phẩm, món ăn hoặc dinh dưỡng. " +
                    "Bạn có thể thử hỏi: 'Cách nấu bí đỏ', 'Rau nào tốt cho sức khỏe?', hoặc 'Mua khoai tây ở đâu?' 🌱";
        }

        String internalReply = searchInternalData(userMessage, productName);
        if (!internalReply.isEmpty()) {
            return internalReply;
        }

        try {
            String gptReply = formatGPTContent(sendToGPT(userMessage));
            return buildFriendlyGPTReply("Dưới đây là thông tin tham khảo", null, gptReply);
        } catch (Exception e) {
            log.error("Error processing query '{}': {}", userMessage, e.getMessage());
            return "⚠️ Xin lỗi, hệ thống đang gặp sự cố. Vui lòng thử lại sau!";
        }
    }

    private String searchInternalData(String query, String productName) {
        String lowerQuery = normalizeProductName(query);
        log.info("Processing query: {}, productName: {}", query, productName);

        // Kiểm tra câu hỏi về sự tồn tại sản phẩm
        if (productName != null && AVAILABILITY_KEYWORDS.stream().anyMatch(lowerQuery::contains)) {
            Optional<Product> product = matchProductByGPTName(productName, productRepository.findAll());
            if (product.isPresent()) {
                Product p = product.get();
                return "Chào bạn! Website hiện có bán sản phẩm <strong>" + p.getProductName() + "</strong>. " +
                        "Xem chi tiết tại <a href='http://localhost:9090/product-details/" + p.getProductId() + "' target='_blank'>đây</a>.";
            } else {
                return "📌 Hiện tại website chưa kinh doanh sản phẩm <strong>" + productName + "</strong>.";
            }
        }

        // Kiểm tra câu hỏi về quy trình mua hàng
        if (PURCHASE_KEYWORDS.stream().anyMatch(lowerQuery::contains)) {
            if (productName != null) {
                Optional<Product> product = matchProductByGPTName(productName, productRepository.findAll());
                if (product.isPresent()) {
                    Product p = product.get();
                    return "Chào bạn! Website hiện có bán sản phẩm <strong>" + p.getProductName() + "</strong>. " +
                            "Xem chi tiết tại <a href='http://localhost:9090/product-details/" + p.getProductId() + "' target='_blank'>đây</a>.";
                } else {
                    return "📌 Hiện tại website chưa kinh doanh sản phẩm <strong>" + productName + "</strong>.";
                }
            }
            return "Để mua sản phẩm trên website, bạn vui lòng làm theo các bước sau: <br><br>" +
                    "<ol>" +
                    "<li><strong>Đăng nhập tài khoản</strong></li>" +
                    "<li><strong>Chọn sản phẩm và cho vào giỏ hàng</strong></li>" +
                    "<li><strong>Vào giỏ hàng và ấn 'Thanh toán'</strong></li>" +
                    "<li><strong>Điền đầy đủ thông tin, kiểm tra đơn hàng và áp dụng khuyến mãi (nếu có)</strong></li>" +
                    "<li><strong>Chọn phương thức thanh toán (ZaloPay, VNPay hoặc COD)</strong></li>" +
                    "<li><strong>Sau khi hoàn tất, bạn sẽ nhận được email xác nhận đơn đặt hàng</strong></li>" +
                    "</ol>" +
                    "Bạn có thể tham khảo chi tiết tại <a href='http://localhost:9090/shop' target='_blank'><strong>liên kết này</strong></a>.";
        }

        if (productName != null || RECIPE_KEYWORDS.stream().anyMatch(lowerQuery::contains)) {
            Optional<Product> product = productName != null ?
                    matchProductByGPTName(productName, productRepository.findAll()) : Optional.empty();

            if (USAGE_KEYWORDS.stream().anyMatch(lowerQuery::contains)) {
                if (product.isPresent()) {
                    Product p = product.get();
                    String usageInfo = p.getDescription();
                    if (usageInfo != null && !usageInfo.isEmpty()) {
                        log.info("Retrieved usageInfo for {} from database", p.getProductName());
                        return buildFriendlyGPTReply("Công dụng của", p.getProductName(), usageInfo);
                    } else {
                        String gptUsage = fetchUsageFromGPT(p.getProductName());
                        p.setDescription(gptUsage);
                        try {
                            productRepository.save(p);
                            log.info("Saved usageInfo for {} to database", p.getProductName());
                        } catch (Exception e) {
                            log.error("Failed to save usageInfo for {}: {}", p.getProductName(), e.getMessage());
                        }
                        return buildFriendlyGPTReply("Công dụng của", p.getProductName(), gptUsage);
                    }
                } else {
                    // Sản phẩm không có trong database, gọi ChatGPT để lấy công dụng
                    String gptUsage = fetchUsageFromGPT(productName);
                    return buildFriendlyGPTReply("Công dụng của", productName, gptUsage);
                }
            } else if (RECIPE_KEYWORDS.stream().anyMatch(lowerQuery::contains)) {
                // Xử lý công thức món ăn
                List<String> dishKeywords = extractDishKeywords(query);
                String primaryDish = dishKeywords.isEmpty() ? null : dishKeywords.get(0);

                List<Blog> blogs = blogRepository.findAll().stream()
                        .filter(b -> {
                            String content = normalizeProductName(b.getMarkdownContent() + " " + b.getTitle());
                            return primaryDish != null && content.contains(primaryDish);
                        })
                        .collect(Collectors.toList());

                if (!blogs.isEmpty()) {
                    String links = blogs.stream()
                            .map(b -> "• <a href='http://localhost:9090/news/" + b.getSlug() + "' target='_blank'>" + b.getTitle() + "</a>")
                            .collect(Collectors.joining("<br>"));
                    log.info("Found {} relevant blogs for dish: {}", blogs.size(), primaryDish);
                    return "🥗 Tham khảo món ngon liên quan:<br>" + links;
                }

                String recipeName = primaryDish != null ? primaryDish : productName;
                if (recipeName != null) {
                    log.info("No relevant blogs found, fetching recipe for: {}", recipeName);
                    return buildFriendlyGPTReply("Cách làm món", recipeName, fetchSimpleRecipeFromGPT(recipeName));
                }
            }

            if (product.isPresent()) {
                Product p = product.get();
                return buildFriendlyGPTReply("Thông tin sản phẩm", p.getProductName(),
                        p.getDescription() + "<br>Xem chi tiết tại " +
                                "<a href='http://localhost:9090/product-details/" + p.getProductId() + "' target='_blank'>liên kết này</a>.");
            } else if (productName != null) {
                return "📌 Hiện tại website chưa kinh doanh sản phẩm <strong>" + productName + "</strong>.";
            }
        }
        return "";
    }




    private String normalizeProductName(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private List<String> extractDishKeywords(String userMessage) {
        List<String> keywords = new ArrayList<>();
        String lowerMessage = normalizeProductName(userMessage);

        String[] dishPatterns = {"bún xào chay", "món chay", "salad", "nước ép", "sinh tố", "cá kho", "phở chay", "gỏi cuốn", "chả giò chay"};
        for (String pattern : dishPatterns) {
            if (lowerMessage.contains(pattern)) {
                keywords.add(pattern);
            }
        }

        if (keywords.isEmpty() && RECIPE_KEYWORDS.stream().anyMatch(lowerMessage::contains)) {
            String prompt = "Hãy trích xuất tên món ăn bằng tiếng Việt từ câu sau, chỉ trả về tên món ăn hoặc 'null' nếu không có. " +
                    "Ví dụ: 'cách làm cá kho' -> 'cá kho'. Câu: \"" + userMessage + "\"";
            String dishName = sendToGPT(prompt).trim();
            if (!dishName.equalsIgnoreCase("null") && !dishName.isEmpty()) {
                keywords.add(dishName);
            }
        }

        return keywords;
    }

    private String extractProductNameLocally(String message) {
        List<String> productNames = productRepository.findAll().stream()
                .map(Product::getProductName)
                .map(this::normalizeProductName)
                .collect(Collectors.toList());
        for (String name : productNames) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(name) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(normalizeProductName(message)).find()) {
                return name;
            }
        }
        return null;
    }

    private String extractProductNameFromGPT(String userMessage) {
        String prompt = "Bạn là trợ lý cho website bán thực phẩm sạch tại Việt Nam. " +
                "Hãy trích xuất chính xác tên sản phẩm thực phẩm (rau, củ, quả, cá, thịt) bằng tiếng Việt có dấu từ câu người dùng nếu có. " +
                "Chỉ trả về tên sản phẩm, không thêm bất kỳ từ nào. Nếu không có sản phẩm nào, chỉ trả về đúng chuỗi: null.\n\n" +
                "Câu: \"" + userMessage + "\"";
        try {
            String result = sendToGPT(prompt).trim();
            if (result.equalsIgnoreCase("null") || result.isEmpty()) return null;
            return normalizeProductName(result);
        } catch (Exception e) {
            return null;
        }
    }

    private Optional<Product> matchProductByGPTName(String gptName, List<Product> products) {
        String gptClean = normalizeProductName(gptName);
        for (Product p : products) {
            String dbName = normalizeProductName(p.getProductName());
            if (dbName.equals(gptClean)) return Optional.of(p);
        }
        for (Product p : products) {
            String dbName = normalizeProductName(p.getProductName());
            if ((dbName.contains(gptClean) || gptClean.contains(dbName)) && gptClean.length() > 4) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    private boolean isRelevantToFood(String message) {
        String lower = normalizeProductName(message);
        return lower.contains("ăn") || lower.contains("món") || lower.contains("thực phẩm")
                || lower.contains("nấu") || lower.contains("rau") || lower.contains("củ")
                || lower.contains("trái cây") || lower.contains("nguyên liệu")
                || lower.contains("chế biến") || lower.contains("mua")
                || lower.contains("thực đơn") || lower.contains("dinh dưỡng")
                || lower.contains("tác dụng") || lower.contains("công dụng")
                || lower.contains("thanh toán") || lower.contains("sức khỏe")
                || lower.contains("vitamin") || lower.contains("khoáng chất")
                || lower.contains("chế độ ăn");
    }

    private String sendToGPT(String prompt) {
        String normalizedPrompt = normalizeProductName(prompt);
        return gptCache.get(normalizedPrompt, p -> {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o");
            requestBody.put("messages", List.of(Map.of("role", "user", "content", p)));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions", entity, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = message.get("content").toString();
            log.info("Fetched from GPT for prompt: {}", p);
            return content;
        });
    }

    private String formatGPTContent(String gptContent) {
        if (gptContent == null || gptContent.trim().isEmpty()) {
            return "Không có thông tin để hiển thị.";
        }
        return gptContent.replaceAll("\n", "<br>")
                .replaceAll("\\. ", ".<br>");
    }

    private String buildFriendlyGPTReply(String title, String name, String content) {
        StringBuilder reply = new StringBuilder();
        reply.append("<p>").append(title);
        if (name != null) {
            reply.append(" <strong>").append(name).append("</strong>");
        }
        reply.append(":</p><p><em>").append(content).append("</em></p>");
        return reply.toString();
    }

    private String fetchSimpleRecipeFromGPT(String productName) {
        String prompt = "Hãy gợi ý một món ăn đơn giản có nguyên liệu là '" + productName + "'. " +
                "Viết ngắn gọn bằng tiếng Việt, liệt kê nguyên liệu và các bước thực hiện.";
        return formatGPTContent(sendToGPT(prompt));
    }

    private String fetchUsageFromGPT(String productName) {
        String normalizedName = normalizeProductName(productName);
        String cacheKey = "usage:" + normalizedName;
        String cachedUsage = gptCache.getIfPresent(cacheKey);
        if (cachedUsage != null) {
            log.info("Retrieved usage for {} from cache", normalizedName);
            return cachedUsage;
        }

        String prompt = "Hãy liệt kê ngắn gọn các công dụng của '" + productName + "' đối với sức khỏe bằng tiếng Việt.";
        String usage = formatGPTContent(sendToGPT(prompt));
        gptCache.put(cacheKey, usage);
        log.info("Fetched and cached usage for {}", normalizedName);
        return usage;
    }
}