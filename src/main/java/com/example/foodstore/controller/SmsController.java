package com.example.foodstore.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

@RestController
public class SmsController {

    private static final String ACCESS_TOKEN_SMS = "_kMgpTvKzBJseM1MeHyNOGKYX8by8DR5";
    private static final String SENDER = "2f8b0e5d0d8d8020";
    private static final int SMS_TYPE = 5; // Gửi SMS qua ứng dụng Android
    public static final String API_URL = "https://api.speedsms.vn/index.php";

    @PostMapping("/send-verification-code")
    public String sendVerificationCode(@RequestParam String phone, HttpSession session) {
        Integer attempts = (Integer) session.getAttribute("smsAttempts");
        Long lastAttemptTime = (Long) session.getAttribute("lastAttemptTime");

        if (attempts == null) attempts = 0;
        if (lastAttemptTime == null) lastAttemptTime = 0L;

        if (attempts >= 3 && (System.currentTimeMillis() - lastAttemptTime) < 5 * 60 * 1000) {
            return "Đã vượt quá số lần gửi mã. Vui lòng thử lại sau 5 phút.";
        }

        if (!phone.matches("^0\\d{9}$")) {
            return "Số điện thoại không hợp lệ";
        }

        String formattedPhone = "+84" + phone.substring(1);

        try {
            String verificationCode = String.valueOf((int) (Math.random() * 900000) + 100000);
            String message = "OTP là: " + verificationCode;

            String json = "{\"to\": [\"" + formattedPhone + "\"], \"content\": \"" + EncodeNonAsciiCharacters(message) + "\", \"type\":" + SMS_TYPE + ", \"brandname\":\"" + SENDER + "\"}";
            URL url = new URL(API_URL + "/sms/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            String userCredentials = ACCESS_TOKEN_SMS + ":x";
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.getBytes());
            conn.setRequestProperty("Authorization", basicAuth);
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(json);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine = "";
                StringBuffer buffer = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    buffer.append(inputLine);
                }
                in.close();

                String response = buffer.toString();
                System.out.println("Phản hồi từ SpeedSMS: " + response);

                if (response.contains("\"status\":\"success\"")) {
                    session.setAttribute("verificationCode", verificationCode);
                    session.setAttribute("phoneToVerify", phone);
                    session.setAttribute("codeCreatedAt", System.currentTimeMillis());
                    session.setAttribute("smsAttempts", attempts + 1);
                    session.setAttribute("lastAttemptTime", System.currentTimeMillis());

                    return "Mã xác thực đã được gửi đến " + phone;
                } else {
                    return "Lỗi khi gửi OTP: " + response;
                }
            } else {
                return "Lỗi khi gửi OTP: HTTP " + responseCode;
            }
        } catch (IOException e) {
            System.err.println("Lỗi SpeedSMS: " + e.getMessage());
            return "Lỗi khi gửi OTP: " + e.getMessage();
        }
    }

    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody Map<String, Object> payload) {
        String type = (String) payload.get("type");

        if ("report".equals(type)) {
            String tranId = String.valueOf(payload.get("tranId"));
            String phone = (String) payload.get("phone");
            String status = String.valueOf(payload.get("status"));

            System.out.println("Delivery Report - TranId: " + tranId + ", Phone: " + phone + ", Status: " + status + " lúc " + new java.util.Date());

            if ("0".equals(status)) {
                System.out.println("OTP đã được gửi thành công đến " + phone);
            } else {
                System.out.println("Gửi OTP thất bại đến " + phone + ", Status: " + status);
            }
        }
    }

    private String EncodeNonAsciiCharacters(String value) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            int unit = (int)c;
            if (unit > 127) {
                String hex = String.format("%04x", (int)unit);
                String encodedValue = "\\u" + hex;
                sb.append(encodedValue);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}