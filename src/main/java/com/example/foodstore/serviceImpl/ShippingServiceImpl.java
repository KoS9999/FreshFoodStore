package com.example.foodstore.serviceImpl;

import com.example.foodstore.dto.ShippingInfo;
import com.example.foodstore.service.ShippingService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
public class ShippingServiceImpl implements ShippingService {

    @Value("${google.api.key}")
    private String apiKey;
    private static final String ORIGIN = "01 Đ. Võ Văn Ngân, Linh Chiểu, Thủ Đức, Hồ Chí Minh, Việt Nam";
    @Override
    public ShippingInfo calculateShipping(String destinationAddress) {
        try {
            String urlString = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="
                    + URLEncoder.encode(ORIGIN, StandardCharsets.UTF_8)
                    + "&destinations=" + URLEncoder.encode(destinationAddress, StandardCharsets.UTF_8)
                    + "&language=vi&key=" + apiKey;

            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String result = response.toString();
            System.out.println("Kết quả trả về từ API: " + result);

            JSONObject json = new JSONObject(result);
            String status = json.optString("status");
            if (!"OK".equals(status)) {
                System.out.println(">>> Lỗi trong response API: " + status);
                return null;
            }

            JSONArray rows = json.optJSONArray("rows");
            JSONArray elements = rows.getJSONObject(0).optJSONArray("elements");
            JSONObject element = elements.getJSONObject(0);
            String durationText = element.optJSONObject("duration").optString("text");
            int distanceMeters = element.optJSONObject("distance").optInt("value", 0);
            double distanceKm = distanceMeters / 1000.0;
            double shippingCost = 15000 + (distanceKm * 5000);

            ShippingInfo info = new ShippingInfo();
            info.setDurationText(durationText);
            info.setDistanceKm(distanceKm);
            info.setShippingCost(shippingCost);

            return info;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
