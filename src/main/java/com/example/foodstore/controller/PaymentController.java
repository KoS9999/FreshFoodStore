package com.example.foodstore.controller;

import com.example.foodstore.dto.ItemDetail;
import com.example.foodstore.entity.*;
import com.example.foodstore.repository.OrderDetailRepository;
import com.example.foodstore.repository.OrderRepository;
import com.example.foodstore.repository.ProductRepository;
import com.example.foodstore.repository.UserRepository;
import com.example.foodstore.service.EmailService;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private UserRepository userRepository;
    private final String appId = "2553";
    private final String key1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
    private static final String key2 = "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz";
    private final String endpoint = "https://sb-openapi.zalopay.vn/v2/create";
    private final String callback_url = "https://f0ad-171-250-162-244.ngrok-free.app/api/payment/callback";

    @PostMapping("/createPayment")
    public ResponseEntity<String> createPayment(
            @RequestParam("app_user") String appUser,
            @RequestParam("totalPrice") String totalPrice,
            @RequestParam("items") String items,
            @RequestParam("embedData") String embedDataJson,
            @RequestParam("redirect_url") String redirectUrl
    ) {
        try {
            System.out.println("Data received from frontend: ");
            System.out.println("app_user: " + appUser);
            System.out.println("totalPrice: " + totalPrice);
            System.out.println("items: " + items);
            System.out.println("embedData: " + embedDataJson);

            double totalAmount = Double.parseDouble(totalPrice);
            long amount = Math.round(totalAmount);
            String appTransId = generateAppTransId();
            long appTime = System.currentTimeMillis();

            JSONArray itemsJsonArray = new JSONArray(items);
            for (int i = 0; i < itemsJsonArray.length(); i++) {
                JSONObject item = itemsJsonArray.getJSONObject(i);
                System.out.println("Item: " + item.toString());
            }

            JSONObject embedData = new JSONObject(embedDataJson);
            embedData.put("redirecturl", redirectUrl);
            System.out.println("Embed Data: " + embedData);

            Map<String, Object> orderData = new HashMap<>();
            orderData.put("app_id", appId);
            orderData.put("app_user", appUser);
            orderData.put("app_trans_id", appTransId);
            orderData.put("app_time", appTime);
            orderData.put("amount", amount);
            orderData.put("embed_data", embedData.toString());
            orderData.put("description", "Thanh toán đơn hàng qua ZaloPay");
            orderData.put("item", itemsJsonArray.toString());
            orderData.put("bank_code", "zalopayapp");
            orderData.put("callback_url", callback_url);

            String macData = appId + "|" + appTransId + "|" + appUser + "|" + amount + "|" + appTime + "|" + embedData + "|" + itemsJsonArray;
            String mac = generateMac(macData, key1);
            orderData.put("mac", mac);

            System.out.println("Order Data (Before Sending to ZaloPay): " + orderData);

            JSONObject response = sendOrderToZaloPay(orderData);

            System.out.println("ZaloPay Response: " + response.toString());
            if (response.getInt("return_code") == 1) {
                String orderUrl = response.getString("order_url");
                return ResponseEntity.status(302)
                        .header("Location", orderUrl)
                        .build();
            } else {
                System.err.println("Error Response from ZaloPay: " + response.toString());
                return ResponseEntity.badRequest().body("Error: " + response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }


    private JSONObject sendOrderToZaloPay(Map<String, Object> orderData) throws Exception {
        HttpPost post = new HttpPost(endpoint);
        List<NameValuePair> params = new ArrayList<>();
        for (Map.Entry<String, Object> entry : orderData.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
        }

        post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String responseString = new String(client.execute(post).getEntity().getContent().readAllBytes(), "UTF-8");
            System.out.println("Payload gửi tới ZaloPay: " + orderData);
            System.out.println("Phản hồi từ ZaloPay: " + responseString);
            return new JSONObject(responseString);
        }
    }

    private String generateAppTransId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String date = sdf.format(new Date());
        String uniqueId = String.valueOf((int) (Math.random() * 1000000));
        return date + "_" + uniqueId;
    }

    private String generateMac(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
        return DatatypeConverter.printHexBinary(mac.doFinal(data.getBytes())).toLowerCase();
    }
    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody String callbackData) {
        try {
            JSONObject callbackJson = new JSONObject(callbackData);

            String dataString = callbackJson.getString("data");
            JSONObject dataJson = new JSONObject(dataString);

            String appUserEmail = dataJson.getString("app_user");
            double amount = dataJson.getDouble("amount");
            String embedDataString = dataJson.getString("embed_data");
            String itemsString = dataJson.getString("item");

            JSONArray itemsArray = new JSONArray(itemsString);
            JSONObject embedData = new JSONObject(embedDataString);

            Optional<User> userOptional = userRepository.findByEmail(appUserEmail);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }
            User user = userOptional.get();

            Order order = new Order();
            order.setOrderDate(new Date());
            order.setAmount(amount);
            order.setAddress(embedData.optString("address", ""));
            order.setPhone(embedData.optString("phone", ""));
            order.setNote(embedData.optString("note", ""));
            order.setUser(user);
            order.setStatus(1);
            order.setOrderStatus(OrderStatus.PENDING);
            orderRepository.save(order);

            List<ItemDetail> itemDetails = new ArrayList<>();
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                Long productId = item.getLong("itemid");
                int quantity = item.getInt("itemquantity");
                double price = item.getDouble("itemprice");

                Optional<Product> productOptional = productRepository.findById(productId);
                if (productOptional.isPresent()) {
                    Product product = productOptional.get();
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setProduct(product);
                    orderDetail.setQuantity(quantity);
                    orderDetail.setPrice(price);
                    orderDetailRepository.save(orderDetail);

                    itemDetails.add(new ItemDetail(product.getProductName(), quantity, price));
                }
            }
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("order", order);
            context.setVariable("items", itemDetails);
            context.setVariable("totalPrice", order.getAmount());

            emailService.sendOrderConfirmationEmail(appUserEmail, "Xác nhận đơn hàng từ Fresh Food", context);

            return ResponseEntity.ok("Callback handled successfully. Email sent.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error handling callback: " + e.getMessage());
        }
    }
    @PostMapping("/createCODOrder")
    public ResponseEntity<String> createCODOrder(
            @RequestParam("app_user") String appUser,
            @RequestParam("totalPrice") String totalPrice,
            @RequestParam("items") String items,
            @RequestParam("embedData") String embedDataJson
    ) {
        try {
            System.out.println("COD Order Data received from frontend: ");
            System.out.println("app_user: " + appUser);
            System.out.println("totalPrice: " + totalPrice);
            System.out.println("items: " + items);
            System.out.println("embedData: " + embedDataJson);

            double totalAmount = Double.parseDouble(totalPrice);
            JSONArray itemsArray = new JSONArray(items);
            JSONObject embedData = new JSONObject(embedDataJson);

            Optional<User> userOptional = userRepository.findByEmail(appUser);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }
            User user = userOptional.get();

            Order order = new Order();
            order.setOrderDate(new Date());
            order.setAmount(totalAmount);
            order.setAddress(embedData.optString("address", ""));
            order.setPhone(embedData.optString("phone", ""));
            order.setNote(embedData.optString("note", ""));
            order.setUser(user);
            order.setStatus(0);
            order.setOrderStatus(OrderStatus.PENDING);
            orderRepository.save(order);

            List<ItemDetail> itemDetails = new ArrayList<>();
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                Long productId = item.getLong("itemid");
                int quantity = item.getInt("itemquantity");
                double price = item.getDouble("itemprice");

                Optional<Product> productOptional = productRepository.findById(productId);
                if (productOptional.isPresent()) {
                    Product product = productOptional.get();
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setProduct(product);
                    orderDetail.setQuantity(quantity);
                    orderDetail.setPrice(price);
                    orderDetailRepository.save(orderDetail);

                    itemDetails.add(new ItemDetail(product.getProductName(), quantity, price));
                }
            }
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("order", order);
            context.setVariable("items", itemDetails);
            context.setVariable("totalPrice", order.getAmount());
            emailService.sendOrderConfirmationEmail(appUser, "Xác nhận đơn hàng COD từ Fresh Food", context);
            return ResponseEntity.status(HttpStatus.FOUND) // 302 redirect
                    .header("Location", "/order-success")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }


    private boolean checkTransactionStatus(String appTransId) {
        try {
            String endpoint = "https://sb-openapi.zalopay.vn/v2/query";
            String macData = appId + "|" + appTransId + "|" + key1;
            String mac = generateMac(macData, key1);
            Map<String, String> queryData = new HashMap<>();
            queryData.put("app_id", appId);
            queryData.put("app_trans_id", appTransId);
            queryData.put("mac", mac);
            HttpPost post = new HttpPost(endpoint);
            List<NameValuePair> params = new ArrayList<>();
            for (Map.Entry<String, String> entry : queryData.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                String responseString = new String(client.execute(post).getEntity().getContent().readAllBytes(), "UTF-8");
                System.out.println("Transaction Status Response: " + responseString);
                JSONObject responseJson = new JSONObject(responseString);
                return responseJson.getInt("return_code") == 1 && responseJson.getInt("sub_return_code") == 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
