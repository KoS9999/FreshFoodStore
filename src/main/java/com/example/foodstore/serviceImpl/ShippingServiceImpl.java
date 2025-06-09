package com.example.foodstore.serviceImpl;

import com.example.foodstore.dto.ShippingInfo;
import com.example.foodstore.entity.Branch;
import com.example.foodstore.repository.BranchRepository;
import com.example.foodstore.service.ShippingService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShippingServiceImpl implements ShippingService {

    @Value("${google.api.key}")
    private String apiKey;

    @Autowired
    private BranchRepository branchRepository;

    @Override
    public ShippingInfo calculateShipping(String destinationAddress) {
        try {
            List<Branch> branches = branchRepository.findAll();
            if (branches.isEmpty()) {
                System.out.println(">>> Không tìm thấy chi nhánh nào.");
                return null;
            }

            String origins = branches.stream()
                    .map(branch -> branch.getLatitude() + "," + branch.getLongitude())
                    .collect(Collectors.joining("|"));

            String urlString = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="
                    + origins
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
            JSONArray rows = json.optJSONArray("rows");

            if (rows == null || rows.length() == 0) {
                System.out.println(">>> Không có kết quả trả về từ Google Maps API.");
                return null;
            }

            double minDistance = Double.MAX_VALUE;
            Branch nearestBranch = null;
            String durationText = "";

            for (int i = 0; i < rows.length(); i++) {
                JSONArray elements = rows.getJSONObject(i).optJSONArray("elements");
                JSONObject element = elements.getJSONObject(0);

                if ("OK".equals(element.optString("status"))) {
                    int distanceMeters = element.optJSONObject("distance").optInt("value", 0);
                    double distanceKm = distanceMeters / 1000.0;

                    if (distanceKm < minDistance) {
                        minDistance = distanceKm;
                        nearestBranch = branches.get(i);
                        durationText = element.optJSONObject("duration").optString("text");
                    }
                }
            }

            if (nearestBranch == null) {
                System.out.println(">>> Không tìm thấy chi nhánh khả dụng.");
                return null;
            }

            double shippingCost = 15000 + (minDistance * 5000);

            // Khởi tạo thông tin giao hàng
            ShippingInfo info = new ShippingInfo();
            info.setDurationText(durationText);
            info.setDistanceKm(minDistance);
            info.setShippingCost(shippingCost);
            info.setBranchName(nearestBranch.getName());
            info.setBranchAddress(nearestBranch.getAddress());

            System.out.println(">>> Chi nhánh gần nhất: " + nearestBranch.getName() + " - " + nearestBranch.getAddress());
            return info;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
