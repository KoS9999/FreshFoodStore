package com.example.foodstore.service;

import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.ViewLog;

import java.util.List;

public interface ViewLogService {
    void cleanOldViewLogs();
    List<Product> getRecentProducts(Long userId);
    void save(ViewLog viewLog);
}
