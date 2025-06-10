package com.example.foodstore.controller;

import com.example.foodstore.dto.ItemDetail;
import com.example.foodstore.dto.VNPayTransactionDTO;
import com.example.foodstore.entity.*;
import com.example.foodstore.repository.*;
import com.example.foodstore.service.EmailService;
import com.example.foodstore.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

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
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;
    private final String appId = "2554"; //2553
    private final String key1 = "sdngKKJmqEMzvh5QQcdD2A9XBSKUNaYn"; //PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL
    private static final String key2 = "trMrHtvjo6myautxDUiAcYsVtaeQ8nhf"; //kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz
    private final String endpoint = "https://sb-openapi.zalopay.vn/v2/create";
    private final String callback_url = "https://3f73-171-252-154-167.ngrok-free.app/api/payment/callback";

    //VNPAY
    private final String vnp_TmnCode = "F0AEHL04";
    private final String vnp_HashSecret = "85ADDC045705FGRWYNL973VABM0QK4N5";
    private final String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private final String vnp_ReturnUrl = "http://localhost:9090/api/payment/vnpay-ipn";

    private static final ConcurrentHashMap<String, VNPayTransactionDTO> transactionStore = new ConcurrentHashMap<>();

    @PostMapping("/createVNPayPayment")
    @Transactional
    public ResponseEntity<Map<String, String>> createVNPayPayment(
            @RequestParam("app_user") String appUser,
            @RequestParam("totalPrice") String totalPrice,
            @RequestParam("items") String items,
            @RequestParam("embedData") String embedDataJson,
            @RequestParam("redirect_url") String redirectUrl,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            @RequestParam(value = "usedPoints", required = false, defaultValue = "0") int usedPoints
    ) {
        try {
            System.out.println("VNPay - Data received from frontend: ");
            System.out.println("app_user: " + appUser);
            System.out.println("totalPrice: " + totalPrice);
            System.out.println("items: " + items);
            System.out.println("embedData: " + embedDataJson);
            System.out.println("Voucher Code: " + voucherCode);

            JSONObject embedData = new JSONObject(embedDataJson);
            JSONArray itemsArray = new JSONArray(items);

            double distanceKm = embedData.optDouble("distanceKm", 0.0);
            if (distanceKm > 30.0) {
                return createErrorResponse("Khoảng cách giao hàng vượt quá 30 km. Vui lòng chọn địa chỉ gần hơn.", HttpStatus.BAD_REQUEST);
            }

            Optional<User> userOptional = userRepository.findByEmail(appUser);
            if (userOptional.isEmpty()) {
                return createErrorResponse("Người dùng không tồn tại.", HttpStatus.NOT_FOUND);
            }
            User user = userOptional.get();

            double shippingCost = embedData.optDouble("shippingCost", 0.0);
            double originalTotal = Double.parseDouble(totalPrice);
            int redeemAmount = (usedPoints / 1000) * 2000;
            double voucherDiscount = 0.0;

            if (voucherCode != null && !voucherCode.isEmpty()) {
                Optional<Voucher> voucherOptional = voucherRepository.findByCode(voucherCode);
                if (voucherOptional.isPresent()) {
                    Voucher voucher = voucherOptional.get();
                    LocalDateTime now = LocalDateTime.now();
                    if (voucher.isStatus() && voucher.getStartDate().isBefore(now) && voucher.getEndDate().isAfter(now)) {
                        voucherDiscount = originalTotal * (voucher.getDiscountPercent() / 100.0);
                        System.out.println("VNPay - Voucher Discount: " + voucherDiscount);
                    } else {
                        return createErrorResponse("Mã voucher không hợp lệ hoặc đã hết hạn.", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return createErrorResponse("Không tìm thấy mã voucher: " + voucherCode, HttpStatus.BAD_REQUEST);
                }
            }
            embedData.put("voucherDiscount", voucherDiscount);

            if (usedPoints > 0) {
                if (user.getPoints() < usedPoints) {
                    return createErrorResponse("Không đủ điểm để quy đổi.", HttpStatus.BAD_REQUEST);
                }
            }

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                Long productId = item.getLong("itemid");
                int quantity = item.getInt("itemquantity");

                Optional<Product> productOptional = productRepository.findByIdWithLock(productId);
                if (productOptional.isEmpty()) {
                    return createErrorResponse("Sản phẩm với ID " + productId + " không tồn tại.", HttpStatus.BAD_REQUEST);
                }
                Product product = productOptional.get();
                if (product.getQuantity() < quantity) {
                    return createErrorResponse("Sản phẩm " + product.getProductName() + " không đủ số lượng tồn kho.", HttpStatus.BAD_REQUEST);
                }
            }

            double finalTotal = originalTotal - redeemAmount - voucherDiscount + shippingCost;
            System.out.println("VNPay - Original Total: " + originalTotal);
            System.out.println("VNPay - Used Points: " + usedPoints);
            System.out.println("VNPay - Redeem Amount (VND): " + redeemAmount);
            System.out.println("VNPay - Voucher Discount (VND): " + voucherDiscount);
            System.out.println("VNPay - Final Total: " + finalTotal);

            String vnp_TxnRef = generateVNPayTxnRef();

            VNPayTransactionDTO transaction = new VNPayTransactionDTO();
            transaction.setTxnRef(vnp_TxnRef);
            transaction.setAppUserEmail(appUser);
            transaction.setEmbedData(embedData.toString());
            transaction.setItems(items);
            transaction.setAmount(finalTotal);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            transaction.setCreatedAt(formatter.format(new Date()));
            transactionStore.put(vnp_TxnRef, transaction);
            System.out.println("VNPay - Transaction stored: TxnRef=" + vnp_TxnRef + ", Transaction=" + transaction);

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(Math.round(finalTotal * 100)));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + vnp_TxnRef + " cho user " + appUser);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", "127.0.0.1"); // Replace with actual client IP in production

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                        hashData.append('&');
                        query.append('&');
                    }
                }
            }

            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(vnp_SecureHash);

            String paymentUrl = vnp_Url + "?" + query.toString();
            System.out.println("VNPay - Payment URL: " + paymentUrl);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("error", "false");
            successResponse.put("message", "Tạo giao dịch VNPay thành công.");
            successResponse.put("order_url", paymentUrl);
            return ResponseEntity.status(HttpStatus.OK).body(successResponse);

        } catch (Exception e) {
            System.err.println("VNPay - Create payment error: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/vnpay-ipn")
    @Transactional
    public Object handleVNPayIPN(@RequestParam Map<String, String> params, HttpServletRequest request) {
        try {
            System.out.println("VNPay IPN - Data received: " + params);

            String vnp_SecureHash = params.get("vnp_SecureHash");
            if (vnp_SecureHash == null) {
                System.out.println("VNPay IPN - Missing vnp_SecureHash");
                return isIPNRequest(request) ? createIPNResponse("97", "Invalid Checksum") : redirectToOrderStatus("error", "Chữ ký không hợp lệ", null);
            }

            Map<String, String> fields = new HashMap<>(params);
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            String signValue = hashAllFields(fields);
            if (!signValue.equals(vnp_SecureHash)) {
                System.out.println("VNPay IPN - Checksum verification failed. Expected: " + vnp_SecureHash + ", Calculated: " + signValue);
                return isIPNRequest(request) ? createIPNResponse("97", "Invalid Checksum") : redirectToOrderStatus("error", "Chữ ký không hợp lệ", null);
            }
            System.out.println("VNPay IPN - Checksum verified successfully");

            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_Amount = params.get("vnp_Amount");
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TransactionStatus = params.get("vnp_TransactionStatus");
            System.out.println("VNPay IPN - Parameters: TxnRef=" + vnp_TxnRef + ", Amount=" + vnp_Amount + ", ResponseCode=" + vnp_ResponseCode + ", TransactionStatus=" + vnp_TransactionStatus);

            VNPayTransactionDTO transaction = transactionStore.get(vnp_TxnRef);
            if (transaction == null) {
                System.out.println("VNPay IPN - Transaction not found in store: " + vnp_TxnRef);
                return isIPNRequest(request) ? createIPNResponse("01", "Order not Found") : redirectToOrderStatus("error", "Không tìm thấy giao dịch", null);
            }
            System.out.println("VNPay IPN - Transaction found: " + transaction);

            double amount = Double.parseDouble(vnp_Amount) / 100;
            if (Math.abs(amount - transaction.getAmount()) > 0.01) {
                System.out.println("VNPay IPN - Invalid amount. Expected: " + transaction.getAmount() + ", Received: " + amount);
                transactionStore.remove(vnp_TxnRef);
                return isIPNRequest(request) ? createIPNResponse("04", "Invalid Amount") : redirectToOrderStatus("error", "Số tiền không hợp lệ", null);
            }
            System.out.println("VNPay IPN - Amount verified: " + amount);

            if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {

                String appUserEmail = transaction.getAppUserEmail();
                JSONObject embedData = new JSONObject(transaction.getEmbedData());
                JSONArray itemsArray = new JSONArray(transaction.getItems());
                System.out.println("VNPay IPN - Extracted data: Email=" + appUserEmail + ", EmbedData=" + embedData + ", Items=" + itemsArray);

                Optional<User> userOptional = userRepository.findByEmail(appUserEmail);
                if (userOptional.isEmpty()) {
                    System.out.println("VNPay IPN - User not found: " + appUserEmail);
                    transactionStore.remove(vnp_TxnRef);
                    return isIPNRequest(request) ? createIPNResponse("01", "Order not Found") : redirectToOrderStatus("error", "Người dùng không tồn tại", null);
                }
                User user = userOptional.get();

                String voucherCode = embedData.optString("voucherCode", null);
                int usedPoints = Integer.parseInt(embedData.optString("usedPoints", "0"));
                double voucherDiscount = embedData.optDouble("voucherDiscount", 0.0);
                double redeemAmount = (usedPoints / 1000) * 2000;

                if (voucherCode != null && !voucherCode.isEmpty()) {
                    Optional<Voucher> voucherOptional = voucherRepository.findByCode(voucherCode);
                    if (voucherOptional.isPresent()) {
                        Voucher voucher = voucherOptional.get();
                        LocalDateTime now = LocalDateTime.now();
                        if (voucher.isStatus() && voucher.getStartDate().isBefore(now) && voucher.getEndDate().isAfter(now)) {
                            System.out.println("VNPay IPN - Voucher validated: " + voucherCode + ", Discount=" + voucherDiscount);
                        } else {
                            System.out.println("VNPay IPN - Invalid voucher: " + voucherCode);
                            transactionStore.remove(vnp_TxnRef);
                            return isIPNRequest(request) ? createIPNResponse("04", "Invalid Voucher") : redirectToOrderStatus("error", "Mã voucher không hợp lệ", null);
                        }
                    } else {
                        System.out.println("VNPay IPN - Voucher not found: " + voucherCode);
                        transactionStore.remove(vnp_TxnRef);
                        return isIPNRequest(request) ? createIPNResponse("04", "Invalid Voucher") : redirectToOrderStatus("error", "Không tìm thấy mã voucher", null);
                    }
                }

                if (usedPoints > 0) {
                    if (user.getPoints() < usedPoints) {
                        transactionStore.remove(vnp_TxnRef);
                        return isIPNRequest(request) ? createIPNResponse("04", "Invalid Points") : redirectToOrderStatus("error", "Không đủ điểm để quy đổi", null);
                    }
                    user.setPoints(user.getPoints() - usedPoints);
                    userRepository.save(user);
                }

                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);
                    Long productId = item.getLong("itemid");
                    int quantity = item.getInt("itemquantity");

                    Optional<Product> productOptional = productRepository.findByIdWithLock(productId);
                    if (productOptional.isEmpty()) {
                        transactionStore.remove(vnp_TxnRef);
                        return isIPNRequest(request) ? createIPNResponse("01", "Product not Found") : redirectToOrderStatus("error", "Sản phẩm không tồn tại", null);
                    }
                    Product product = productOptional.get();
                    if (product.getQuantity() < quantity) {
                        transactionStore.remove(vnp_TxnRef);
                        return isIPNRequest(request) ? createIPNResponse("04", "Insufficient Product Quantity") : redirectToOrderStatus("error", "Sản phẩm " + product.getProductName() + " không đủ số lượng", null);
                    }
                    product.setQuantity(product.getQuantity() - quantity);
                    productRepository.save(product);
                }

                Order order = new Order();
                order.setOrderDate(new Date());
                order.setAmount(amount);
                order.setAddress(embedData.optString("address", ""));
                order.setPhone(embedData.optString("phone", ""));
                order.setNote(embedData.optString("note", ""));
                order.setShippingCost(embedData.optDouble("shippingCost", 0.0));
                order.setDurationText(embedData.optString("estimatedTime", ""));
                order.setDistanceKm(embedData.optDouble("distanceKm", 0.0));
                order.setUser(user);
                order.setStatus(1); // Success
                order.setOrderStatus(OrderStatus.PENDING);
                order.setUsedPoints(usedPoints);
                order.setVoucherDiscount(voucherDiscount);
                order.setRedeemAmount(redeemAmount);

                if (voucherCode != null && !voucherCode.isEmpty()) {
                    order.setVoucherCode(voucherCode);
                }
                orderRepository.save(order);

                notificationService.sendNewOrderNotification("/topic/admin", "Có đơn hàng mới từ khách hàng " + user.getEmail());

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

                transactionStore.remove(vnp_TxnRef);

                return isIPNRequest(request) ? createIPNResponse("00", "Confirm Success") : redirectToOrderStatus("success", "Giao dịch thành công", vnp_TxnRef);
            } else {
                transactionStore.remove(vnp_TxnRef);
                String errorMessage = getResponseCodeMessage(vnp_ResponseCode);
                return isIPNRequest(request) ? createIPNResponse("00", "Confirm Success") : redirectToOrderStatus("error", errorMessage, null);
            }
        } catch (Exception e) {
            System.err.println("VNPay IPN - Error: " + e.getMessage());
            e.printStackTrace();
            return isIPNRequest(request) ? createIPNResponse("99", "Unknown error") : redirectToOrderStatus("error", "Lỗi không xác định", null);
        }
    }

    private boolean isIPNRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent == null || !userAgent.contains("Mozilla");
    }

    private ResponseEntity<Void> redirectToOrderStatus(String status, String message, String txnRef) {
        System.out.println("Redirecting to /order-status with status=" + status + ", message=" + message + ", txnRef=" + txnRef);
        StringBuilder redirectUrl = new StringBuilder("/order-status?status=");
        redirectUrl.append(URLEncoder.encode(status, StandardCharsets.UTF_8));
        redirectUrl.append("&message=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));
        if (txnRef != null) {
            redirectUrl.append("&txnRef=").append(URLEncoder.encode(txnRef, StandardCharsets.UTF_8));
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl.toString())
                .build();
    }

    private String hashAllFields(Map<String, String> fields) throws Exception {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    hashData.append('&');
                }
            }
        }
        return hmacSHA512(vnp_HashSecret, hashData.toString());
    }

    private String hmacSHA512(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(hmacData).toLowerCase();
    }

    private ResponseEntity<Map<String, String>> createIPNResponse(String rspCode, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("RspCode", rspCode);
        response.put("Message", message);
        return ResponseEntity.ok(response);
    }

    private String getResponseCodeMessage(String responseCode) {
        switch (responseCode) {
            case "00": return "Giao dịch thành công";
            case "07": return "Giao dịch bị nghi ngờ gian lận";
            case "09": return "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking";
            case "10": return "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11": return "Đã hết hạn chờ thanh toán";
            case "12": return "Thẻ/Tài khoản bị khóa";
            case "13": return "Nhập sai mật khẩu xác thực (OTP)";
            case "24": return "Khách hàng hủy giao dịch";
            case "51": return "Tài khoản không đủ số dư";
            case "65": return "Tài khoản vượt quá hạn mức giao dịch trong ngày";
            case "75": return "Ngân hàng thanh toán đang bảo trì";
            case "79": return "Nhập sai mật khẩu thanh toán quá số lần quy định";
            case "99": return "Lỗi không xác định";
            default: return "Lỗi không xác định";
        }
    }


    private String generateVNPayTxnRef() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String date = sdf.format(new Date());
        String uniqueId = String.valueOf((int) (Math.random() * 1000000));
        return date + "_" + uniqueId;
    }
    @Scheduled(fixedRate = 86400000)
    public void cleanupTransactionStore() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        long now = System.currentTimeMillis();
        transactionStore.entrySet().removeIf(entry -> {
            try {
                long created = formatter.parse(entry.getValue().getCreatedAt()).getTime();
                return (now - created) > 86400000; // Remove after 24 hours
            } catch (Exception e) {
                return true; // Remove if parsing fails
            }
        });
        System.out.println("VNPay - Transaction store cleaned up. Remaining transactions: " + transactionStore.size());
    }





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
        System.out.println("TotalPrice " + totalPrice);

        double discountAmount = totalPrice * (voucher.getDiscountPercent() / 100);
        System.out.println("DiscountAmount " + discountAmount);

        response.put("message", "Mã giảm giá hợp lệ!");
        response.put("discountAmount", discountAmount);

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
            response.put("message", "Bạn không đủ điểm tích lũy để áp dụng ưu đãi này.");
            response.put("currentPoints", currentPoints);
            response.put("maxRedeemableVND", maxRedeemableVND);
            return ResponseEntity.badRequest().body(response);
        }

        int requiredPoints = (redeemAmountInVND * 1000) / 2000;


        response.put("message", "Đã quy đổi thành công.");
        response.put("usedPoints", requiredPoints);
        response.put("redeemAmountVND", redeemAmountInVND);
        response.put("remainingPoints", user.getPoints());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/createCODOrder")
    @Transactional
    public ResponseEntity<Map<String, String>> createCODOrder(
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

            JSONArray itemsArray = new JSONArray(items);
            JSONObject embedData = new JSONObject(embedDataJson);

            double distanceKm = embedData.optDouble("distanceKm", 0.0);
            if (distanceKm > 30.0) {
                return createErrorResponse("Khoảng cách giao hàng vượt quá 30 km. Vui lòng chọn địa chỉ gần hơn.", HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userRepository.findByEmail(appUser);
            if (userOptional.isEmpty()) {
                return createErrorResponse("Người dùng không tồn tại.", HttpStatus.NOT_FOUND);
            }
            User user = userOptional.get();

            double shippingCost = embedData.optDouble("shippingCost", 0.0);
            double originalTotal = Double.parseDouble(totalPrice);
            int redeemAmount = (usedPoints / 1000) * 2000;
            double voucherDiscount = 0.0;

            if (voucherCode != null && !voucherCode.isEmpty()) {
                Optional<Voucher> voucherOptional = voucherRepository.findByCode(voucherCode);
                if (voucherOptional.isPresent()) {
                    Voucher voucher = voucherOptional.get();
                    LocalDateTime now = LocalDateTime.now();
                    if (voucher.isStatus() && voucher.getStartDate().isBefore(now) && voucher.getEndDate().isAfter(now)) {
                        voucherDiscount = originalTotal * (voucher.getDiscountPercent() / 100.0);
                        System.out.println("Voucher Discount: " + voucherDiscount);
                    } else {
                        return createErrorResponse("Mã voucher không hợp lệ hoặc đã hết hạn.", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return createErrorResponse("Không tìm thấy mã voucher: " + voucherCode, HttpStatus.BAD_REQUEST);
                }
            }

            if (usedPoints > 0) {
                if (user.getPoints() < usedPoints) {
                    return createErrorResponse("Không đủ điểm để quy đổi.", HttpStatus.BAD_REQUEST);
                }
                user.setPoints(user.getPoints() - usedPoints);
                userRepository.save(user);
            }

            double finalAmount = originalTotal - redeemAmount - voucherDiscount + shippingCost;

            Order order = new Order();
            order.setOrderDate(new Date());
            order.setAmount(finalAmount);
            order.setAddress(embedData.optString("address", ""));
            order.setPhone(embedData.optString("phone", ""));
            order.setNote(embedData.optString("note", ""));
            order.setShippingCost(embedData.optDouble("shippingCost", 0.0));
            order.setDurationText(embedData.optString("estimatedTime", ""));
            order.setDistanceKm(embedData.optDouble("distanceKm", 0.0));
            order.setUser(user);
            order.setStatus(0);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setUsedPoints(usedPoints);
            order.setVoucherDiscount(voucherDiscount);
            order.setRedeemAmount((double) redeemAmount);

            if (voucherCode != null && !voucherCode.isEmpty()) {
                Optional<Voucher> voucherOpt = voucherRepository.findByCodeIgnoreCase(voucherCode);
                if (voucherOpt.isPresent()) {
                    order.setVoucherCode(voucherCode);
                }
            }

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                Long productId = item.getLong("itemid");
                int quantity = item.getInt("itemquantity");

                Optional<Product> productOptional = productRepository.findByIdWithLock(productId);
                if (productOptional.isEmpty()) {
                    return createErrorResponse("Sản phẩm với ID " + productId + " không tồn tại.", HttpStatus.BAD_REQUEST);
                }
                Product product = productOptional.get();
                if (product.getQuantity() < quantity) {
                    return createErrorResponse("Sản phẩm " + product.getProductName() + " không đủ số lượng tồn kho.", HttpStatus.BAD_REQUEST);
                }
            }

            orderRepository.save(order);

            notificationService.sendNewOrderNotification("/topic/admin", "Có đơn hàng mới từ khách hàng " + user.getEmail());

            List<ItemDetail> itemDetails = new ArrayList<>();
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                Long productId = item.getLong("itemid");
                int quantity = item.getInt("itemquantity");
                double price = item.getDouble("itemprice");

                Optional<Product> productOptional = productRepository.findByIdWithLock(productId);
                if (productOptional.isPresent()) {
                    Product product = productOptional.get();
                    product.setQuantity(product.getQuantity() - quantity);
                    if (product.getQuantity() == 0) {
                        product.setStatus(false); // Đánh dấu sản phẩm hết hàng
                    }
                    productRepository.save(product);

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

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("error", "false");
            successResponse.put("message", "Đơn hàng COD được tạo thành công.");
            successResponse.put("redirectUrl", "/order-success");
            return ResponseEntity.status(HttpStatus.OK).body(successResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/createPayment")
    @Transactional
    public ResponseEntity<Map<String, String>> createPayment(
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

            JSONObject embedData = new JSONObject(embedDataJson);
            JSONArray itemsArray = new JSONArray(items);

            double distanceKm = embedData.optDouble("distanceKm", 0.0);
            if (distanceKm > 30.0) {
                return createErrorResponse("Khoảng cách giao hàng vượt quá 30 km. Vui lòng chọn địa chỉ gần hơn.", HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userRepository.findByEmail(appUser);
            if (userOptional.isEmpty()) {
                return createErrorResponse("Người dùng không tồn tại.", HttpStatus.NOT_FOUND);
            }
            User user = userOptional.get();

            double shippingCost = embedData.optDouble("shippingCost", 0.0);
            double originalTotal = Double.parseDouble(totalPrice);
            int redeemAmount = (usedPoints / 1000) * 2000;
            double voucherDiscount = 0.0;

            if (voucherCode != null && !voucherCode.isEmpty()) {
                Optional<Voucher> voucherOptional = voucherRepository.findByCode(voucherCode);
                if (voucherOptional.isPresent()) {
                    Voucher voucher = voucherOptional.get();
                    LocalDateTime now = LocalDateTime.now();
                    if (voucher.isStatus() && voucher.getStartDate().isBefore(now) && voucher.getEndDate().isAfter(now)) {
                        voucherDiscount = originalTotal * (voucher.getDiscountPercent() / 100.0);
                        System.out.println("Voucher Discount: " + voucherDiscount);
                    } else {
                        return createErrorResponse("Mã voucher không hợp lệ hoặc đã hết hạn.", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return createErrorResponse("Không tìm thấy mã voucher: " + voucherCode, HttpStatus.BAD_REQUEST);
                }
            }

            if (usedPoints > 0) {
                if (user.getPoints() < usedPoints) {
                    return createErrorResponse("Không đủ điểm để quy đổi.", HttpStatus.BAD_REQUEST);
                }
            }

            double finalTotal = originalTotal - redeemAmount - voucherDiscount + shippingCost;

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                Long productId = item.getLong("itemid");
                int quantity = item.getInt("itemquantity");

                Optional<Product> productOptional = productRepository.findByIdWithLock(productId);
                if (productOptional.isEmpty()) {
                    return createErrorResponse("Sản phẩm với ID " + productId + " không tồn tại.", HttpStatus.BAD_REQUEST);
                }
                Product product = productOptional.get();
                if (product.getQuantity() < quantity) {
                    return createErrorResponse("Sản phẩm " + product.getProductName() + " không đủ số lượng tồn kho.", HttpStatus.BAD_REQUEST);
                }
            }

            System.out.println("ZaloPay - Original Total: " + originalTotal);
            System.out.println("ZaloPay - Used Points: " + usedPoints);
            System.out.println("ZaloPay - Redeem Amount (VND): " + redeemAmount);
            System.out.println("ZaloPay - Voucher Discount (VND): " + voucherDiscount);
            System.out.println("ZaloPay - Final Total Sent to ZaloPay: " + finalTotal);

            long amount = Math.round(finalTotal);
            String appTransId = generateAppTransId();
            long appTime = System.currentTimeMillis();

            embedData.put("redirecturl", redirectUrl);
            embedData.put("voucherCode", voucherCode);
            embedData.put("usedPoints", usedPoints + "");
            embedData.put("voucherDiscount", voucherDiscount);
            embedData.put("redeemAmount", redeemAmount);

            System.out.println("Embed Data: " + embedData);

            Map<String, Object> orderData = new HashMap<>();
            orderData.put("app_id", appId);
            orderData.put("app_user", appUser);
            orderData.put("app_trans_id", appTransId);
            orderData.put("app_time", appTime);
            orderData.put("amount", amount);
            orderData.put("embed_data", embedData.toString());
            orderData.put("description", "Thanh toán đơn hàng qua ZaloPay");
            orderData.put("item", itemsArray.toString());
            orderData.put("bank_code", "");
            orderData.put("callback_url", callback_url);

            String macData = appId + "|" + appTransId + "|" + appUser + "|" + amount + "|" + appTime + "|" + embedData + "|" + itemsArray;
            String mac = generateMac(macData, key1);
            orderData.put("mac", mac);

            System.out.println("Order Data (Before Sending to ZaloPay): " + orderData);

            JSONObject response = sendOrderToZaloPay(orderData);

            System.out.println("ZaloPay Response: " + response.toString());
            if (response.getInt("return_code") == 1) {
                Map<String, String> successResponse = new HashMap<>();
                successResponse.put("error", "false");
                successResponse.put("message", "Tạo giao dịch ZaloPay thành công.");
                successResponse.put("order_url", response.getString("order_url"));
                return ResponseEntity.status(HttpStatus.OK).body(successResponse);
            } else {
                return createErrorResponse("Lỗi từ ZaloPay: " + response.getString("return_message"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/callback")
    @Transactional
    public ResponseEntity<Map<String, String>> handleCallback(@RequestBody String callbackData) {
        try {
            // Parse dữ liệu callback
            JSONObject callbackJson = new JSONObject(callbackData);
            String dataString = callbackJson.getString("data");
            String reqMac = callbackJson.getString("mac");

            String computedMac = generateMac(dataString, key2);
            if (!reqMac.equals(computedMac)) {
                return createErrorResponse("Callback không hợp lệ: MAC không khớp", HttpStatus.BAD_REQUEST);
            }
            // Nếu MAC hợp lệ, tiếp tục xử lý
            JSONObject dataJson = new JSONObject(dataString);
            String appUserEmail = dataJson.getString("app_user");
            double amount = dataJson.getDouble("amount");
            String embedDataString = dataJson.getString("embed_data");
            String itemsString = dataJson.getString("item");

            JSONObject embedData = new JSONObject(embedDataString);
            JSONArray itemsArray = new JSONArray(itemsString);

            Optional<User> userOptional = userRepository.findByEmail(appUserEmail);
            if (userOptional.isEmpty()) {
                return createErrorResponse("Người dùng không tồn tại.", HttpStatus.NOT_FOUND);
            }
            User user = userOptional.get();

            String voucherCode = embedData.optString("voucherCode", null);
            double voucherDiscount = embedData.optDouble("voucherDiscount", 0.0);
            double redeemAmount = embedData.optDouble("redeemAmount", 0.0);
            int usedPoints = Integer.parseInt(embedData.optString("usedPoints", "0"));

            if (usedPoints > 0) {
                if (user.getPoints() < usedPoints) {
                    return createErrorResponse("Không đủ điểm để quy đổi.", HttpStatus.BAD_REQUEST);
                }
                user.setPoints(user.getPoints() - usedPoints);
                userRepository.save(user);
            }

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                Long productId = item.getLong("itemid");
                int quantity = item.getInt("itemquantity");

                Optional<Product> productOptional = productRepository.findByIdWithLock(productId);
                if (productOptional.isEmpty()) {
                    return createErrorResponse("Sản phẩm với ID " + productId + " không tồn tại.", HttpStatus.BAD_REQUEST);
                }
                Product product = productOptional.get();
                if (product.getQuantity() < quantity) {
                    return createErrorResponse("Sản phẩm " + product.getProductName() + " không đủ số lượng tồn kho.", HttpStatus.BAD_REQUEST);
                }
            }

            Order order = new Order();
            order.setOrderDate(new Date());
            order.setAmount(amount);
            order.setAddress(embedData.optString("address", ""));
            order.setPhone(embedData.optString("phone", ""));
            order.setNote(embedData.optString("note", ""));
            order.setShippingCost(embedData.optDouble("shippingCost", 0.0));
            order.setDurationText(embedData.optString("estimatedTime", ""));
            order.setDistanceKm(embedData.optDouble("distanceKm", 0.0));
            order.setUser(user);
            order.setStatus(1);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setUsedPoints(usedPoints);
            order.setVoucherDiscount(voucherDiscount);
            order.setRedeemAmount(redeemAmount);

            if (voucherCode != null && !voucherCode.isEmpty()) {
                order.setVoucherCode(voucherCode);
            }
            orderRepository.save(order);

            notificationService.sendNewOrderNotification("/topic/admin", "Có đơn hàng mới từ khách hàng " + user.getEmail());

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

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("return_code", "1");
            successResponse.put("return_message", "Callback xử lý thành công. Email xác nhận đã được gửi.");
            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("return_code", "0");
            errorResponse.put("return_message", "Lỗi xử lý callback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private ResponseEntity<Map<String, String>> createErrorResponse(String message, HttpStatus status) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "true");
        errorResponse.put("message", message);
        return ResponseEntity.status(status).body(errorResponse);
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
