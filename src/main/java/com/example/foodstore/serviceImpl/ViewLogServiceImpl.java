package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.ViewLog;
import com.example.foodstore.repository.ViewLogRepository;
import com.example.foodstore.service.ViewLogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViewLogServiceImpl implements ViewLogService{
    private final ViewLogRepository viewLogRepository;

    public ViewLogServiceImpl(ViewLogRepository viewLogRepository) {
        this.viewLogRepository = viewLogRepository;
    }

    @Override
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanOldViewLogs() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        Date cutoffDate = cal.getTime();
        viewLogRepository.deleteByViewTimeBefore(cutoffDate);
    }

    @Override
    public List<Product> getRecentProducts(Long userId) {
        List<ViewLog> viewLogs = viewLogRepository.findTop11ByUserUserIdOrderByViewTimeDesc(userId);
        System.out.println("Fetched " + viewLogs.size() + " ViewLogs for userId: " + userId);
        // Chuyển đổi sang danh sách Product, loại bỏ trùng lặp dựa trên productId
        List<Product> products = viewLogs.stream()
                .map(ViewLog::getProduct)
                .distinct()
                .limit(11)
                .collect(Collectors.toList());

        for (Product product : products) {
            System.out.println("Product ID: " + product.getProductId() +
                    ", Product Name: " + product.getProductName());
        }

        return products;
    }
    @Override
    public void save(ViewLog viewLog) {
        viewLogRepository.save(viewLog);
    }
}
