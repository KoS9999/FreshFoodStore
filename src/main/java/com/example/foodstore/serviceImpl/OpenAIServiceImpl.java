package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Blog;
import com.example.foodstore.entity.Product;
import com.example.foodstore.repository.BlogRepository;
import com.example.foodstore.repository.ProductRepository;
import com.example.foodstore.service.OpenAIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final ProductRepository productRepository;
    private final BlogRepository blogRepository;

    public OpenAIServiceImpl(ProductRepository productRepository, BlogRepository blogRepository) {
        this.productRepository = productRepository;
        this.blogRepository = blogRepository;
    }

    @Override
    public String getChatBotReply(String userMessage) {
        String productName = extractProductNameFromGPT(userMessage);

        if (!isRelevantToFood(userMessage) && productName == null) {
            return "🤖 Xin lỗi, mình chỉ hỗ trợ các nội dung liên quan đến thực phẩm sạch, sản phẩm, món ăn hoặc dinh dưỡng. " +
                    "Bạn có thể thử hỏi: 'Cách nấu bí đỏ', 'Rau nào tốt cho sức khỏe?', hoặc 'Mua khoai tây ở đâu?' 🌱";
        }

        String internalReply = searchInternalData(userMessage);
        if (!internalReply.isEmpty()) return internalReply;

        try {
            String gptReply = sendToGPT(userMessage);

            Optional<Product> matched = productName != null ?
                    matchProductByGPTName(productName, productRepository.findAll()) : Optional.empty();

            if (productName != null && matched.isEmpty()) {
                return "📌 Hiện tại website chưa kinh doanh sản phẩm <strong>" + productName + "</strong>.<br>"
                        + "Tuy nhiên, bạn có thể tham khảo thêm thông tin sau:<br><br><em>" + gptReply + "</em>";
            }

            return buildFriendlyGPTReply("Dưới đây là một số thông tin bạn có thể tham khảo", null, gptReply);

        } catch (Exception e) {
            return "⚠️ Xin lỗi, hệ thống đang gặp sự cố. Vui lòng thử lại sau!";
        }
    }

    private String searchInternalData(String query) {
        String lowerQuery = query.toLowerCase();

        if (lowerQuery.contains("thanh toán") || lowerQuery.contains("giỏ hàng") || lowerQuery.contains("cách mua hàng")) {
            return "✅ Để mua sản phẩm trên website, bạn vui lòng làm theo các bước sau: <br><br>"
                    + "<ol>"
                    + "<li><strong>Đăng nhập tài khoản</strong></li>"
                    + "<li><strong>Chọn sản phẩm và cho vào giỏ hàng</strong></li>"
                    + "<li><strong>Vào giỏ hàng và ấn 'Checkout'</strong></li>"
                    + "<li><strong>Điền đầy đủ thông tin, kiểm tra đơn hàng và áp dụng khuyến mãi (nếu có)</strong></li>"
                    + "<li><strong>Chọn phương thức thanh toán (COD hoặc ZaloPay)</strong></li>"
                    + "<li><strong>Sau khi hoàn tất, bạn sẽ nhận được email xác nhận đơn đặt hàng</strong></li>"
                    + "</ol>"
                    + "Bạn có thể tham khảo chi tiết về các sản phẩm tại "
                    + "<a href='http://localhost:9090/shop' target='_blank'><strong>liên kết này</strong></a>.";
        }

        String productName = extractProductNameFromGPT(query);
        if (productName == null) return "";

        List<Product> products = productRepository.findAll();
        Optional<Product> matched = matchProductByGPTName(productName, products);

        if (matched.isPresent()) {
            Product p = matched.get();
            String name = p.getProductName();

            if (lowerQuery.contains("mua") || lowerQuery.contains("bán") || lowerQuery.contains("đặt") || lowerQuery.contains("giá")) {
                return "✅ Website hiện có bán sản phẩm <strong>" + name + "</strong>. "
                        + "Bạn có thể xem chi tiết tại "
                        + "<a href='http://localhost:9090/product-details/" + p.getProductId()
                        + "' target='_blank'>liên kết này</a>.";
            }

            if (lowerQuery.contains("công dụng") || lowerQuery.contains("tác dụng")) {
                return buildFriendlyGPTReply("Công dụng của sản phẩm", name, p.getDescription());
            }

            if (lowerQuery.contains("món") || lowerQuery.contains("nấu") || lowerQuery.contains("kết hợp") ||
                    lowerQuery.contains("cách làm") || lowerQuery.contains("bài viết") || lowerQuery.contains("công thức")) {
                List<Blog> blogs = blogRepository.findAll().stream()
                        .filter(b -> {
                            String content = (b.getMarkdownContent() + " " + b.getTitle()).toLowerCase();
                            return content.contains(name.toLowerCase());
                        })
                        .collect(Collectors.toList());

                if (!blogs.isEmpty()) {
                    String links = blogs.stream()
                            .map(b -> "• <a href='http://localhost:9090/news/" + b.getSlug()
                                    + "' target='_blank'>" + b.getTitle() + "</a>")
                            .collect(Collectors.joining("<br>"));

                    return "🥗 Bạn có thể tham khảo món ngon từ <strong>" + name + "</strong> dưới đây nhé:<br>" + links;
                }

                String gptRecipe = fetchSimpleRecipeFromGPT(name);
                return buildFriendlyGPTReply("Bạn có thể tham khảo về món ăn với", name, gptRecipe);
            }

            return buildFriendlyGPTReply("Thông tin sản phẩm", name,
                    p.getDescription() + "<br>Xem chi tiết tại "
                            + "<a href='http://localhost:9090/product-details/" + p.getProductId()
                            + "' target='_blank'>liên kết này</a>.");
        }

        return "";
    }

    private Optional<Product> matchProductByGPTName(String gptName, List<Product> products) {
        String gptClean = gptName.trim().toLowerCase();

        for (Product p : products) {
            String dbName = p.getProductName().trim().toLowerCase();
            if (dbName.equals(gptClean)) return Optional.of(p);
        }

        for (Product p : products) {
            String dbName = p.getProductName().trim().toLowerCase();
            if ((dbName.contains(gptClean) || gptClean.contains(dbName)) && gptClean.length() > 4) {
                return Optional.of(p);
            }
        }

        return Optional.empty();
    }

    private String buildFriendlyGPTReply(String title, String name, String content) {
        return title + (name != null ? " <strong>" + name + "</strong>:" : ":") + "<br><br><em>" + content + "</em>";
    }

    private String sendToGPT(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                entity,
                Map.class
        );

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return message.get("content").toString();
    }

    private String extractProductNameFromGPT(String userMessage) {
        String prompt = "Bạn là trợ lý cho website bán thực phẩm sạch tại Việt Nam. " +
                "Hãy trích xuất chính xác tên sản phẩm thực phẩm (rau, củ, quả, cá, thịt) bằng tiếng Việt có dấu từ câu người dùng nếu có. " +
                "Chỉ trả về tên sản phẩm, không thêm bất kỳ từ nào. Nếu không có sản phẩm nào, chỉ trả về đúng chuỗi: null.\n\n" +
                "Câu: \"" + userMessage + "\"";

        try {
            String result = sendToGPT(prompt).trim();
            if (result.equalsIgnoreCase("null") || result.isEmpty()) return null;
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isRelevantToFood(String message) {
        String lower = message.toLowerCase();
        return lower.contains("ăn") || lower.contains("món") || lower.contains("thực phẩm")
                || lower.contains("nấu") || lower.contains("rau") || lower.contains("củ")
                || lower.contains("trái cây") || lower.contains("nguyên liệu")
                || lower.contains("chế biến") || lower.contains("mua")
                || lower.contains("thực đơn") || lower.contains("dinh dưỡng")
                || lower.contains("tác dụng") || lower.contains("công dụng")
                || lower.contains("thanh toán");
    }

    private String fetchSimpleRecipeFromGPT(String productName) {
        String prompt = "Hãy gợi ý một món ăn đơn giản có nguyên liệu là '" + productName + "'. " +
                "Viết ngắn gọn bằng tiếng Việt, liệt kê nguyên liệu và các bước thực hiện.";
        return sendToGPT(prompt);
    }
}