package com.example.foodstore.controller;

import com.example.foodstore.dto.ItemDetail;
import com.example.foodstore.entity.*;
import com.example.foodstore.repository.*;
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

import java.time.LocalDateTime;
import java.util.Optional;
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;
    private final String appId = "2553";
    private final String key1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
    private static final String key2 = "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz";
    private final String endpoint = "https://sb-openapi.zalopay.vn/v2/create";
    private final String callback_url = "https://2825-171-252-155-41.ngrok-free.app/api/payment/callback";


    @PostMapping("/check-voucher")
    public ResponseEntity<Map<String, Object>> checkVoucher(@RequestParam String code, @RequestParam Double totalPrice) {
        System.out.println("Nhận mã giảm giá: [" + code + "]");

        Optional<Voucher> voucherOpt = voucherRepository.findByCodeIgnoreCase(code.trim());
        Map<String, Object> response = new HashMap<>();

        if (voucherOpt.isEmpty()) {
            response.put("message", "Mã giảm giá không tồn tại");
            return ResponseEntity.badRequest().body(response);
        }

        Voucher voucher = voucherOpt.get();
        System.out.println("Trạng thái voucher: " + voucher.isStatus());
        System.out.println("Ngày hết hạn: " + voucher.getEndDate());

        if (!voucher.isStatus() || voucher.getEndDate().isBefore(LocalDateTime.now())) {
            response.put("message", "Mã giảm giá đã hết hạn");
            return ResponseEntity.badRequest().body(response);
        }

        double discountAmount = totalPrice * (voucher.getDiscountPercent() / 100);
        double newTotal = totalPrice - discountAmount;

        response.put("message", "Mã giảm giá hợp lệ!");
        response.put("discountAmount", discountAmount);
        response.put("newTotal", newTotal);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemPoints(@RequestParam String email, @RequestParam int redeemAmountInVND) {
        Map<String, Object> response = new HashMap<>();

        if (redeemAmountInVND <= 0) {
            response.put("message", "Số tiền quy đổi phải lớn hơn 0.");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            response.put("message", "Không tìm thấy người dùng.");
            return ResponseEntity.status(404).body(response);
        }

        User user = userOpt.get();
        int currentPoints = user.getPoints();
        int maxRedeemableVND = (currentPoints / 1000) * 2000;

        if (redeemAmountInVND > maxRedeemableVND) {
            response.put("message", "Không đủ điểm để quy đổi số tiền yêu cầu.");
            response.put("currentPoints", currentPoints);
            response.put("maxRedeemableVND", maxRedeemableVND);
            return ResponseEntity.badRequest().body(response);
        }

        int requiredPoints = (redeemAmountInVND * 1000) / 2000;
        user.setPoints(currentPoints - requiredPoints);
        userRepository.save(user);

        response.put("message", "Đã quy đổi thành công.");
        response.put("usedPoints", requiredPoints);
        response.put("redeemAmountVND", redeemAmountInVND);
        response.put("remainingPoints", user.getPoints());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/createCODOrder")
    public ResponseEntity<String> createCODOrder(
            @RequestParam("app_user") String appUser,
            @RequestParam("totalPrice") String totalPrice,
            @RequestParam("items") String items,
            @RequestParam("embedData") String embedDataJson,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            @RequestParam(value = "usedPoints", required = false, defaultValue = "0") int usedPoints
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

            double originalTotal = Double.parseDouble(totalPrice);
            int redeemAmount = (usedPoints / 1000) * 2000;

            double finalAmount = originalTotal;

            if (usedPoints > 0) {
                if (user.getPoints() < usedPoints) {
                    return ResponseEntity.badRequest().body("Không đủ điểm để quy đổi.");
                }
                finalAmount -= redeemAmount;
                user.setPoints(user.getPoints() - usedPoints);
                userRepository.save(user);
            }

            Order order = new Order();
            order.setOrderDate(new Date());
            order.setAmount(finalAmount);
            order.setAddress(embedData.optString("address", ""));
            order.setPhone(embedData.optString("phone", ""));
            order.setNote(embedData.optString("note", ""));
            order.setUser(user);
            order.setStatus(0);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setUsedPoints(usedPoints);

            if (voucherCode != null && !voucherCode.isEmpty()) {
                Optional<Voucher> voucherOpt = voucherRepository.findByCodeIgnoreCase(voucherCode);
                if (voucherOpt.isPresent()) {
                    Voucher voucher = voucherOpt.get();
                    if (voucher.isStatus() && voucher.getEndDate().isAfter(LocalDateTime.now())) {
                        order.setVoucherCode(voucherCode);
                    }
                }
            }

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
            System.out.println("Original total: " + originalTotal);
            System.out.println("Used points: " + usedPoints);
            System.out.println("Redeem amount: " + redeemAmount);
            System.out.println("Final total saved to order: " + finalAmount);
            return ResponseEntity.status(HttpStatus.FOUND) // 302 redirect
                    .header("Location", "/order-success")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping("/createPayment")
    public ResponseEntity<String> createPayment(
            @RequestParam("app_user") String appUser,
            @RequestParam("totalPrice") String totalPrice,
            @RequestParam("items") String items,
            @RequestParam("embedData") String embedDataJson,
            @RequestParam("redirect_url") String redirectUrl,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            @RequestParam(value = "usedPoints", required = false, defaultValue = "0") int usedPoints

    ) {
        try {
            System.out.println("Data received from frontend: ");
            System.out.println("app_user: " + appUser);
            System.out.println("totalPrice: " + totalPrice);
            System.out.println("items: " + items);
            System.out.println("embedData: " + embedDataJson);
            System.out.println("Voucher Code: " + voucherCode);


            double originalTotal = Double.parseDouble(totalPrice);
            int redeemAmount = (usedPoints / 1000) * 2000;
            double finalTotal = originalTotal - redeemAmount;

            System.out.println("ZaloPay - Original Total: " + originalTotal);
            System.out.println("ZaloPay - Used Points: " + usedPoints);
            System.out.println("ZaloPay - Redeem Amount (VND): " + redeemAmount);
            System.out.println("ZaloPay - Final Total Sent to ZaloPay: " + finalTotal);

            long amount = Math.round(finalTotal);

            String appTransId = generateAppTransId();
            long appTime = System.currentTimeMillis();

            JSONArray itemsJsonArray = new JSONArray(items);
            for (int i = 0; i < itemsJsonArray.length(); i++) {
                JSONObject item = itemsJsonArray.getJSONObject(i);
                System.out.println("Item: " + item.toString());
            }

            JSONObject embedData = new JSONObject(embedDataJson);
            embedData.put("redirecturl", redirectUrl);
            embedData.put("voucherCode", voucherCode);
            embedData.put("usedPoints", usedPoints + "");
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

            JSONObject embedData = new JSONObject(embedDataString);
            JSONArray itemsArray = new JSONArray(itemsString);

            Optional<User> userOptional = userRepository.findByEmail(appUserEmail);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }
            User user = userOptional.get();
            String voucherCode = embedData.optString("voucherCode", null);
            int usedPoints = Integer.parseInt(embedData.optString("usedPoints", "0"));
            if (usedPoints > 0) {
                if (user.getPoints() < usedPoints) {
                    return ResponseEntity.badRequest().body("Không đủ điểm để quy đổi.");
                }
                user.setPoints(user.getPoints() - usedPoints);
                userRepository.save(user);
            }

            Order order = new Order();
            order.setOrderDate(new Date());
            order.setAmount(amount);
            order.setAddress(embedData.optString("address", ""));
            order.setPhone(embedData.optString("phone", ""));
            order.setNote(embedData.optString("note", ""));
            order.setUser(user);
            order.setStatus(1);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setUsedPoints(usedPoints);
            if (voucherCode != null && !voucherCode.isEmpty()) {
                order.setVoucherCode(voucherCode);
            }
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
